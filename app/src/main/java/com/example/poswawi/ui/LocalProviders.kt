package com.example.poswawi.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.poswawi.data.CafeRepository

val LocalCafeRepository = staticCompositionLocalOf<CafeRepository> {
    error("CafeRepository not provided")
}
