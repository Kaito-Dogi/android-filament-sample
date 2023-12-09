package app.doggy.filamentsample.di

import com.google.android.filament.Engine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
private object FilamentModule {

  @Provides
  @Singleton
  fun providesEngine(): Engine = Engine.create()
}
