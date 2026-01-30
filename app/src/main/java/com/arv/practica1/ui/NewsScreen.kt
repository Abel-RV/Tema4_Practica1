package com.arv.practica1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arv.practica1.model.Noticia
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel,apiKey:String){
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val fuente by viewModel.fuente.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val pais by viewModel.pais.collectAsStateWithLifecycle()

    var mostrarConfig by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.cargarNoticas(apiKey)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrarConfig) "Configuración" else "Noticias") },
                actions = {
                    IconButton(onClick = { mostrarConfig = !mostrarConfig }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!mostrarConfig) {
                FloatingActionButton(onClick = { viewModel.cargarNoticas(apiKey) }) {
                    Icon(Icons.Default.Refresh, "Refrescar")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (mostrarConfig) {
                // Pantalla de Configuración (Integrada aquí para no crear clases/ficheros nuevos)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Filtros de Búsqueda", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { viewModel.actualizarFiltros(it, fuente, categoria, pais) },
                        label = { Text("Texto (q)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fuente,
                        onValueChange = { viewModel.actualizarFiltros(busqueda, it, categoria, pais) },
                        label = { Text("Fuente ID (ej: techcrunch, google-news)") },
                        supportingText = { Text("Si usas Fuente, se ignora País y Categoría") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Solo habilitar País/Categoría si no hay fuente
                    val habilitarResto = fuente.isBlank()

                    OutlinedTextField(
                        value = pais,
                        onValueChange = { viewModel.actualizarFiltros(busqueda, fuente, categoria, it) },
                        label = { Text("País (ej: us, es, gb)") },
                        enabled = habilitarResto,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { viewModel.actualizarFiltros(busqueda, fuente, it, pais) },
                        label = { Text("Categoría (ej: technology, sports)") },
                        enabled = habilitarResto,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.cargarNoticas(apiKey)
                            mostrarConfig = false // Volver a la lista
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Buscar Noticias")
                    }
                }
            } else {
                // Lista de Noticias
                when (val currentEstado = estado) {
                    is EstadoNoticias.Cargando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is EstadoNoticias.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(currentEstado.mensaje)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.cargarNoticas(apiKey) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    is EstadoNoticias.Exito -> {
                        if (currentEstado.noticias.isEmpty()) {
                            Text("No se encontraron noticias.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn {
                                items(currentEstado.noticias) { noticia ->
                                    NoticiaItem(noticia = noticia)
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NoticiaItem(noticia: Noticia){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if(!noticia.urlToImage.isNullOrBlank()){
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(noticia.urlToImage)
                .crossfade(true)
                .build(),
                contentDescription = "Imagen de la noticia",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = noticia.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = noticia.descripcion?:"",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}