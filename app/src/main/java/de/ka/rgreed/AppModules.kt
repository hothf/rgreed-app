package de.ka.rgreed

import de.ka.rgreed.repo.*
import de.ka.rgreed.repo.api.ApiService
import de.ka.rgreed.repo.db.AppDatabase
import de.ka.rgreed.ui.home.HomeViewModel
import de.ka.rgreed.ui.consensus.consensusdetail.ConsensusDetailViewModel
import de.ka.rgreed.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionViewModel
import de.ka.rgreed.ui.MainViewModel
import de.ka.rgreed.ui.neweditconsensus.NewEditConsensusViewModel
import de.ka.rgreed.ui.personal.PersonalViewModel
import de.ka.rgreed.ui.profile.ProfileViewModel
import de.ka.rgreed.ui.profile.register.RegisterViewModel
import de.ka.rgreed.ui.search.SearchDetailViewModel
import de.ka.rgreed.ui.search.SearchViewModel
import de.ka.rgreed.ui.settings.SettingsViewModel
import de.ka.rgreed.utils.ApiErrorManager
import de.ka.rgreed.utils.BackPressEventListener
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

    single { ApiErrorManager() }
    single { AppDatabase(get()) }
    single { BackPressEventListener() }
    single { ProfileManagerImpl(db = get()) }
    single { ApiService(get(), profileManager = get()) }
    single { RepositoryImpl(get(), get(), get(), get(), get(), get()) as Repository }
    single { ConsensusManagerImpl(api = get(), apiErrorHandler = get(), searchManager = get()) }
    single { SearchManagerImpl(db = get(), api = get(), apiErrorHandler = get()) as SearchManager }
}