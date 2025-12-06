package com.example.dydemo.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 应用模块，负责提供 Repository 和其他全局单例
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // AppRepository 会被 Hilt 自动通过其构造函数注入，
    // 如果它的所有依赖 (UserDao, MessageDao, Context) 都已在其他模块中提供，
    // 那么这里就不需要显式提供 @Provides fun provideAppRepository()。

    // MessageDispatcher 同样可以由 Hilt 自动注入，
    // 因为它的依赖 AppRepository 是可注入的。
}
