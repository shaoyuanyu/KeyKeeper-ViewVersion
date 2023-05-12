package com.ysy.keykeeper.activities.basic_activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

open abstract class MySecureActivity : AppCompatActivity() {

    // 生物验证相关
    lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    lateinit var promptInfo: BiometricPrompt.PromptInfo

    // app前后台状态相关
    object appState {
        var resumed = false
    }

    /**
     * 安全化封装onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 内置生物验证
        generateBiometricProtector()

        myOnCreate()
    }

    /**
     * 用于重写onCreate()
     */
    open fun myOnCreate() { }


    /**
     * 安全化封装onStart()
     */
    override fun onStart() {
        super.onStart()

        // 从后台恢复后必须验证
        if (appState.resumed) {
            // 页面高斯模糊
            blurryProcess()

            activateProtector()
        }

        myOnStart()
    }

    /**
     * 用于重写onStart()
     */
    open fun myOnStart() { }

    /**
     * 用于进行高斯模糊
     */
    open fun blurryProcess() {

    }

    /**
     * 用于解除高斯模糊
     */
    open fun blurryDissolve() {

    }

    /**
     * 初始化生物信息验证
     */
    fun generateBiometricProtector() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError( errorCode: Int, errString: CharSequence ) {
                    super.onAuthenticationError(errorCode, errString)

                    if ( errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED || errorCode == BiometricPrompt.ERROR_TIMEOUT) {
                        activateProtector()
                    } else if ( errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ) {
                        // 退出

                    } else {
                        Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onAuthenticationSucceeded ( result: BiometricPrompt.AuthenticationResult ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "验证成功!", Toast.LENGTH_SHORT).show()
                    appState.resumed = false // app状态更新
                    blurryDissolve() // 解除高斯模糊
                    passAuthentication() // 调用通过验证函数
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "验证失败", Toast.LENGTH_SHORT).show()
                }
            } )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("KeyKeeper生物识别保护")
            .setSubtitle("验证您的指纹以进入KeyKeeper")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText("退出")
            .build()
    }

    /**
     * 验证通过后调用
     */
    open fun passAuthentication() { }

    /**
     * 激活生物验证
     */
    fun activateProtector() {
        Log.i("BiometricProtector: ", "指纹验证")
        biometricPrompt.authenticate(promptInfo)
    }

}