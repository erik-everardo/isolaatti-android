package com.isolaatti.markdown

import android.content.Context
import com.isolaatti.BuildConfig
import com.isolaatti.common.CoilImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute
import io.noties.markwon.linkify.LinkifyPlugin

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideMarkwon(@ApplicationContext context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder
                        .imageDestinationProcessor(
                            ImageDestinationProcessorRelativeToAbsolute
                                .create(BuildConfig.backend))
                }
            })
            .usePlugin(LinkifyPlugin.create())
            .build()
    }
}