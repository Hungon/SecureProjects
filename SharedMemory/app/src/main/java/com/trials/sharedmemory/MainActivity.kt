package com.trials.sharedmemory

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.trials.sharedmemory.services.ShmService
import java.nio.ByteBuffer
import android.os.Bundle
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Messenger
import android.os.IBinder
import android.content.ServiceConnection
import android.system.ErrnoException
import android.system.OsConstants.PROT_READ
import android.system.OsConstants.PROT_WRITE
import android.widget.Toast
import com.trials.sharedmemory.services.PackageCertification
import com.trials.sharedmemory.services.SignaturePermission
import android.os.SharedMemory


class MainActivity : AppCompatActivity() {

    // to send data to Service
    private var mServiceMessenger: Messenger? = null
    // common memory
    private var myShared1: SharedMemory? = null
    private var myShared2: SharedMemory? = null
    // to map memory
    private var mBuf1: ByteBuffer? = null
    private var mBuf2: ByteBuffer? = null
    // will be true as connected to Service
    private var mIsBound = false

    // to receive data from service
    private var mLocalMessenger: Messenger? = null
    private var mServiceConnection: ServiceConnection? = null


    // to establish connection between Activity and Service
    private inner class MyServiceConnection : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mServiceMessenger = Messenger(service)
            sendMessageToService(ShmService.MSG_ATTACH)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mIsBound = false
            mServiceMessenger = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // handle data shared from Service
        val handler = Handler { msg ->
            when (msg.what) {
                ShmService.SHMEM1 -> {
                    myShared1 = msg.obj as SharedMemory
                    useSHMEM1()
                }
                ShmService.SHMEM2 -> {
                    myShared2 = msg.obj as SharedMemory
                    useSHMEM2()
                }
                ShmService.MSG_END -> {
                    releaseAllocatedMemory()
                }
                else -> Log.e(TAG, "invalid message: " + msg.what)
            }
            true
        }
        mLocalMessenger = Messenger(handler)
        doBindService()
    }


    // to connect to Service
    private fun doBindService() {
        mServiceConnection = MyServiceConnection()
        if (!mIsBound) {
            // verify if this certification is owned
            if (!SignaturePermission.test(this, MY_PERMISSION, myCertificationHash)) {
                Toast.makeText(this, "独自定義 Signature Permission が自社アプリにより定義されていない。", Toast.LENGTH_LONG).show()
                return
            }
            if (!PackageCertification.test(this, SHM_PACKAGE, myCertificationHash)) {
                Toast.makeText(this, "利用先サービスは自社アプリではない。", Toast.LENGTH_LONG).show()
                return
            }
        }
        val intent = Intent()
        intent.putExtra("PARAM", "sending sensitive info")
        // bind to common memory with explicit intent
        intent.setClassName(SHM_PACKAGE, SHM_CLASS)
        mServiceConnection?.let {
            if (!bindService(intent, it, Context.BIND_AUTO_CREATE)) {
                Toast.makeText(this, "Bind Service Failed", Toast.LENGTH_LONG).show()
                return
            }
        }
        mIsBound = true
    }

    private fun releaseService() {
        mServiceConnection?.let {
            unbindService(it)
        }
    }

    private fun useSHMEM1() {
        // it throws Exception when invalid shared memory is detected
        if (ShmService.mSHMem1 != null) {
            mBuf1 = mapMemory(
                ShmService.mSHMem1!!,
                PROT_READ,
                ShmService.SHMEM1_BUF1_OFFSET,
                ShmService.SHMEM1_BUF1_LENGTH
            )
            // get data shared
            val len = mBuf1?.getInt() ?: 0
            val bytes = ByteArray(len)
            mBuf1?.get(bytes)
            val message = String(bytes)
            Toast.makeText(this, "Got: $message", Toast.LENGTH_LONG).show()
            sendMessageToService(ShmService.MSG_REPLY1)
            sendMessageToService(ShmService.MSG_ATTACH2)
        }
    }

    private fun useSHMEM2() {
        // it throws Exception when invalid shared memory is detected
        ShmService.mSHMem2?.let {
            mBuf2 = mapMemory(it, PROT_WRITE, ShmService.SHMEM2_BUF1_OFFSET, ShmService.SHMEM2_BUF1_LENGTH)
            if (mBuf2 != null) {
                val size = mBuf2?.getInt() ?: 0
                val bytes = ByteArray(size)
                mBuf2?.get(bytes)
                val msg = String(bytes)
                Log.d(TAG, "Got a message from service: $msg")
                val replyStr = "OK Thanks!"
                mBuf2?.putInt(replyStr.length)
                mBuf2?.put(replyStr.toByteArray())
                sendMessageToService(ShmService.MSG_REPLY2)
            }
        }
    }

    // map specific common memory
    private fun mapMemory(mem: SharedMemory, proto: Int, offset: Int, length: Int): ByteBuffer? {
        return try {
            mem.map(proto, offset, length)
        } catch (e: ErrnoException) {
            Log.e(
                TAG,
                "could not map, proto: $proto, offset: $offset, length: $length\n " + e.message + " err no. = " + e.errno
            )
            return null
        }
    }

    private fun sendMessageToService(what: Int) {
        try {
            val msg = Message.obtain()
            msg.what = what
            msg.replyTo = mLocalMessenger
            mServiceMessenger?.send(msg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error in sending message: ${e.message}")
        }

    }

    private fun releaseAllocatedMemory() {
        sendMessageToService(ShmService.MSG_DETACH)
        // unmap buffer
        if (mBuf1 != null) SharedMemory.unmap(mBuf1)
        if (mBuf2 != null) SharedMemory.unmap(mBuf2)
        // close memory
        myShared1?.close()
        myShared2?.close()
        mBuf1 = null
        mBuf2 = null
        myShared1 = null
        myShared2 = null
        releaseService()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        // info about client Activity
        private const val SHM_PACKAGE = "com.trials.sharedmemory"
        private const val SHM_CLASS = "com.trials.sharedmemory.services.ShmService"
        // Permission name
        private const val MY_PERMISSION = "com.trials.sharedmemory.services.MY_PERMISSION"
        // own certification
        private var myCertificationHash: String? = null
            get() {
                return if (BuildConfig.DEBUG) {
                    // hash code of android.debug as sha256
                    "fb57f3c56192d21dc44bbeca2aae33038e630ce3967c0cf9da6d3d2e40c1908d"
                } else {
                    // hash code of the project as sha256
                    "1210c7e8195664bf3d49ceeecf9f53d1256f4c5cafd43fb27166fd4c44e1c417"
                }
            }

    }
}
