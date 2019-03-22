package com.trials.sharedmemory.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.trials.sharedmemory.BuildConfig
import java.nio.ByteBuffer
import android.system.ErrnoException
import android.system.OsConstants.PROT_READ
import android.system.OsConstants.PROT_WRITE


class ShmService : Service() {

    private var m1Buffer1: ByteBuffer? = null
    private var m2Buffer1: ByteBuffer? = null
    private var m2Buffer2: ByteBuffer? = null
    private var mBufferMapped = false
    private var mMessenger: Messenger? = null


    override fun onCreate() {
        super.onCreate()
        // allocate common memory as creation of instance
        // and then mapping memory
        if (allocateSharedMemory()) {
            mapMemory()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        val handler = Handler { msg ->
            when (msg.what) {
                MSG_ATTACH -> shareWith1(msg)
                MSG_ATTACH2 -> shareWith2(msg)
                MSG_DETACH -> unShare(msg)
                MSG_REPLY1 -> gotReply(msg)
                MSG_REPLY2 -> gotReply2(msg)
                else -> invalidMsg(msg)
            }
            true
        }
        mMessenger = Messenger(handler)
        if (!SignaturePermission.test(this, MY_PERMISSION, myCertificationHash)) {
            Toast.makeText(this, "this unique signature has not been created by own company.", Toast.LENGTH_LONG).show()
            return null
        }
        val param = intent?.getStringExtra("PARAM")
        Log.d(TAG, String.format("Received Param[%s]", param))
        return mMessenger!!.binder
    }

    private fun allocateSharedMemory(): Boolean {
        try {
            mSHMem1 = SharedMemory.create("SHM", PAGE_SIZE)
            mSHMem2 = SharedMemory.create("SHM2", PAGE_SIZE * 2)
        } catch (e: ErrnoException) {
            Log.e(TAG, "failed to allocate shared memory" + e.message)
            return false
        }

        return true
    }

    private fun mapShared(mem: SharedMemory, prot: Int, offset: Int, size: Int): ByteBuffer? {
        return try {
            mem.map(prot, offset, size)
        } catch (e: ErrnoException) {
            Log.e(
                TAG,
                "could not map, proto: $prot, offset: $offset, length: $size\n " + e.message + " err no. = " + e.errno
            )
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "map failed: ${e.message}")
            null
        }
    }

    private fun mapMemory() {
        m1Buffer1 = mapShared(
            mSHMem1!!,
            PROT_READ or PROT_WRITE,
            SHMEM1_BUF1_OFFSET,
            SHMEM1_BUF1_LENGTH
        )
        m2Buffer1 = mapShared(
            mSHMem2!!,
            PROT_READ or PROT_WRITE,
            SHMEM2_BUF1_OFFSET,
            SHMEM2_BUF1_LENGTH
        ); m2Buffer2 = mapShared(mSHMem2!!, PROT_READ or PROT_WRITE, SHMEM2_BUF2_OFFSET, SHMEM2_BUF2_LENGTH);
        if (m1Buffer1 != null && m2Buffer1 != null && m2Buffer2 != null) mBufferMapped = true
    }

    private fun releaseAllocateSharedMemory() {
        if (mBufferMapped) {
            if (mSHMem1 != null) {
                if (m1Buffer1 != null) SharedMemory.unmap(m1Buffer1!!);
                m1Buffer1 = null
                mSHMem1!!.close()
                mSHMem1 = null
            }
            if (mSHMem2 != null) {
                if (m2Buffer1 != null) SharedMemory.unmap(m2Buffer1!!)
                if (m2Buffer2 != null) SharedMemory.unmap(m2Buffer2!!)
                m2Buffer1 = null;
                m2Buffer2 = null
                mSHMem2!!.close()
                mSHMem2 = null
            }
            mBufferMapped = false;
        }
    }

    private fun shareWith1(msg: Message) {
        if (!mBufferMapped) return
        mSHMem1?.setProtect(PROT_READ)
        m1Buffer1?.putInt(greeting.length)
        m1Buffer1?.put(greeting.toByteArray())
        try {
            val sMsg = Message.obtain(null, SHMEM1, mSHMem1)
            msg.replyTo.send(sMsg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to share" + e.message)
        }
    }

    private fun shareWith2(msg: Message) {
        if (!mBufferMapped) return
        mSHMem2?.setProtect(PROT_WRITE);
        m2Buffer1?.putInt(greeting2.length)
        m2Buffer1?.put(greeting2.toByteArray())
        m2Buffer2?.putInt(greeting3.length)
        m2Buffer2?.put(greeting3.toByteArray())
        try {
            val sMsg = Message.obtain(null, SHMEM2, mSHMem2)
            msg.replyTo.send(sMsg);
        } catch (e: RemoteException) {
            Log.e(TAG, "failed to share mSHMem2" + e.message)
        }
    }

    private fun unShare(msg: Message) {
        Log.d(TAG, "unShare() ${msg.what}")
        releaseAllocateSharedMemory()
    }

    private fun invalidMsg(msg: Message) {
        Log.e(TAG, "Got an Invalid message: " + msg.what)
    }

    private fun extractReply(buf: ByteBuffer): String {
        val len = buf.int
        val bytes = ByteArray(len)
        buf.get(bytes)
        return String(bytes)
    }

    private fun gotReply(msg: Message) {
        m1Buffer1?.rewind()
        val message = extractReply(m1Buffer1!!)
        if (message != greeting) {
            Log.e(TAG, "my message was overwritten: $message")
        }
    }

    private fun gotReply2(msg: Message) {
        m2Buffer1?.rewind()
        val message = extractReply(m2Buffer1!!)
        Log.d(
            TAG, "got a message of length: ${message.length} from client: $message")
        val eMsg = Message.obtain()
        eMsg.what = MSG_END
        try {
            msg.replyTo.send(eMsg)
        } catch (e: RemoteException) {
            Log.e(TAG, "error in reply 2: " + e.message)
        }
    }


    companion object {
        private val TAG = ShmService::class.java.simpleName
        // signature permission
        private const val MY_PERMISSION = "com.trials.sharedmemory.services.MY_PERMISSION"
        private const val greeting = "Hi! I send you my memory. Let's Share it!"
        private const val greeting2 = "You can write here!"
        private const val greeting3 = "From this point, I'll also write."
        const val PAGE_SIZE = 1024 * 4
        const val SHMEM1 = 0
        const val SHMEM2 = 1
        const val MSG_INVALID = Integer.MIN_VALUE
        const val MSG_ATTACH = MSG_INVALID + 1
        const val MSG_ATTACH2 = MSG_ATTACH + 1
        const val MSG_DETACH = MSG_ATTACH2 + 1
        const val MSG_DETACH2 = MSG_DETACH + 1
        const val MSG_REPLY1 = MSG_DETACH2 + 1
        const val MSG_REPLY2 = MSG_REPLY1 + 1
        const val MSG_END = MSG_REPLY2 + 1
        const val SHMEM1_BUF1_OFFSET = 0
        const val SHMEM1_BUF1_LENGTH = 1024
        const val SHMEM2_BUF1_OFFSET = 0
        const val SHMEM2_BUF1_LENGTH = 1024
        const val SHMEM2_BUF2_OFFSET = PAGE_SIZE
        const val SHMEM2_BUF2_LENGTH = 128

        var mSHMem1: SharedMemory? = null
        var mSHMem2: SharedMemory? = null

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