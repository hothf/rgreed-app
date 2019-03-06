package de.ka.skyfallapp

import de.ka.skyfallapp.repo.ProfileManagerImpl
import de.ka.skyfallapp.repo.RepositoryImpl
import de.ka.skyfallapp.repo.Repository
import de.ka.skyfallapp.repo.api.ApiService
import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.repo.ConsensusManagerImpl
import de.ka.skyfallapp.ui.home.HomeViewModel
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailViewModel
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionViewModel
import de.ka.skyfallapp.ui.MainViewModel
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusViewModel
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.ui.profile.ProfileViewModel
import de.ka.skyfallapp.ui.settings.SettingsViewModel
import de.ka.skyfallapp.utils.ApiErrorHandler
import de.ka.skyfallapp.utils.BackPressInterceptor
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Declares all modules used for koin dependency injection.
 */

val appModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ConsensusDetailViewModel(get()) }
    viewModel { NewEditSuggestionViewModel(get()) }
    viewModel { NewEditConsensusViewModel(get()) }
    viewModel { PersonalViewModel(get()) }

    single { BackPressInterceptor() }
    single { ApiErrorHandler() }

    single { AppDatabase(get()) }
    single { ProfileManagerImpl(db = get()) }
    single { ApiService(get(), profileManager = get()) }
    single { ConsensusManagerImpl(api = get()) }
    single { RepositoryImpl(get(), get(), get(), get(), get()) as Repository }
}