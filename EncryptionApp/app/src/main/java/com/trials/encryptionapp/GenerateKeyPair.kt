package com.trials.encryptionapp

import java.security.KeyPair
import java.security.KeyPairGenerator

class GenerateKeyPair {

    companion object {

        fun generate(keySize: Int): KeyPair {
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(keySize)
            return kpg.generateKeyPair()
        }
    }
}