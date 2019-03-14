package de.ka.skyfallapp

import de.ka.skyfallapp.repo.*
import de.ka.skyfallapp.repo.api.ApiService
import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.ui.home.HomeViewModel
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailViewModel
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionViewModel
import de.ka.skyfallapp.ui.MainViewModel
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusViewModel
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.ui.profile.ProfileViewModel
import de.ka.skyfallapp.ui.profile.register.RegisterViewModel
import de.ka.skyfallapp.ui.search.SearchDetailViewModel
import de.ka.skyfallapp.ui.search.SearchViewModel
import de.ka.skyfallapp.ui.settings.SettingsViewModel
import de.ka.skyfallapp.utils.ApiErrorHandler
import de.ka.skyfallapp.utils.BackPressEventListener
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Declares all modules used for koin dependency injection.
 */

val appModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { PersonalViewModel(get()) }
    viewModel { SearchDetailViewModel(get()) }
    viewModel { ConsensusDetailViewModel(get()) }
    viewModel { NewEditConsensusViewModel(get()) }
    viewModel { NewEditSuggestionViewModel(get()) }

    single { ApiErrorHandler() }
    single { AppDatabase(get()) }
    single { BackPressEventListener() }
    single { ProfileManagerImpl(db = get()) }
    single { SearchManagerImpl(db = get(), api = get()) as SearchManager }
    single { ApiService(get(), profileManager = get()) }
    single { ConsensusManagerImpl(api = get(), searchManager = get()) }
    single { RepositoryImpl(get(), get(), get(), get(), get()) as Repository }
}