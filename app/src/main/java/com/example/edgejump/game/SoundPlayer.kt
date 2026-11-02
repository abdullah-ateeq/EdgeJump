package com.example.edgejump.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundPlayer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 22050

    var isSoundEnabled: Boolean = true

    fun playJump() {
        if (!isSoundEnabled) return
        scope.launch {
            // Rising chirp: 360Hz -> 720Hz in 80ms
            val durationMs = 80
            val samples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val t = i.toFloat() / samples
                val freq = 360f + 360f * t
                val phase = 2f * PI.toFloat() * freq * (i.toFloat() / sampleRate)
                val envelope = 1f - t
                buffer[i] = (sin(phase) * envelope * Short.MAX_VALUE * 0.45f).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playLand(streak: Int = 0) {
        if (!isSoundEnabled) return
        scope.launch {
            // Pentatonic ascending chime based on streak
            val notes = floatArrayOf(523.25f, 587.33f, 659.25f, 783.99f, 880.00f, 1046.50f)
            val noteIndex = (streak % notes.size)
            val freq = notes[noteIndex]
            val durationMs = 120
            val samples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val t = i.toFloat() / samples
                val phase = 2f * PI.toFloat() * freq * (i.toFloat() / sampleRate)
                val envelope = (1f - t) * (1f - t) // quadratic decay
                buffer[i] = (sin(phase) * envelope * Short.MAX_VALUE * 0.5f).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playMiss() {
        if (!isSoundEnabled) return
        scope.launch {
            // Downward discordant buzz: 280Hz -> 100Hz in 220ms
            val durationMs = 220
            val samples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val t = i.toFloat() / samples
                val freq = 280f - 180f * t
                val phase = 2f * PI.toFloat() * freq * (i.toFloat() / sampleRate)
                val squareDistort = if (sin(phase) > 0) 1f else -1f
                val envelope = 1f - t
                buffer[i] = (squareDistort * envelope * Short.MAX_VALUE * 0.4f).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Track cleans up automatically after playback or on scope release
        } catch (_: Exception) {
            // Ignore audio device access issues gracefully
        }
    }
}
