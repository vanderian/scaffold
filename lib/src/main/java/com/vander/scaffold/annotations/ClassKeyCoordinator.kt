package com.vander.scaffold.annotations

import com.vander.scaffold.screen.Coordinator
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * @author marian on 5.9.2017.
 */
@MapKey
annotation class ClassKeyCoordinator(val value: KClass<out Coordinator>)
