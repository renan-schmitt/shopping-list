package br.com.mentealerta.listadecompras.di

import org.koin.core.module.Module

/**
 * Banco local e preferências, que dependem de onde cada plataforma guarda arquivos: dá o
 * `AppDatabase` e o `DataStore<Preferences>` como `single`. Entra no [sharedModule].
 */
internal expect val platformDataModule: Module
