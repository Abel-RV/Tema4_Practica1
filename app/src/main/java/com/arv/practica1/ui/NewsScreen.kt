package com.arv.practica1.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel, apiKey: String) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Filtros
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val fuente by viewModel.fuente.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val pais by viewModel.pais.collectAsStateWithLifecycle()
    val idioma by viewModel.idioma.collectAsStateWithLifecycle()

    var mostrarConfig by remember { mutableStateOf(false) }

    // ¡NUEVO! Necesario para abrir el navegador
    val context = LocalContext.current

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
                // PANTALLA DE CONFIGURACIÓN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Filtros de Búsqueda", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { viewModel.actualizarFiltros(it, fuente, categoria, pais, idioma) },
                        label = { Text("Texto (q)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fuente,
                        onValueChange = { viewModel.actualizarFiltros(busqueda, it, categoria, pais, idioma) },
                        label = { Text("Fuente ID (ej: wired)") },
                        supportingText = { Text("Incompatible con País, Categoría e Idioma") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val habilitarResto = fuente.isBlank()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pais,
                            onValueChange = { viewModel.actualizarFiltros(busqueda, fuente, categoria, it, idioma) },
                            label = { Text("País (ej: es)") },
                            enabled = habilitarResto,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = idioma,
                            onValueChange = { viewModel.actualizarFiltros(busqueda, fuente, categoria, pais, it) },
                            label = { Text("Idioma (ej: es)") },
                            enabled = habilitarResto,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { viewModel.actualizarFiltros(busqueda, fuente, it, pais, idioma) },
                        label = { Text("Categoría (ej: sports)") },
                        enabled = habilitarResto,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.cargarNoticas(apiKey)
                            mostrarConfig = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Buscar Noticias")
                    }
                }
            } else {
                // LISTA DE NOTICIAS
                when (val currentEstado = estado) {
                    is EstadoNoticias.Cargando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is EstadoNoticias.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Error: ${currentEstado.mensaje}", color = MaterialTheme.colorScheme.error)
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
                                    // ¡NUEVO! Pasamos la lógica del click aquí
                                    NoticiaItem(
                                        noticia = noticia,
                                        onClick = {
                                            if (noticia.url.isNotBlank()) {
                                                // Intent implícito para abrir navegador
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    )
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
fun NoticiaItem(
    noticia: Noticia,
    onClick: () -> Unit // ¡NUEVO! Recibimos la función
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // ¡NUEVO! Detectamos el click en toda la fila
            .padding(16.dp)
    ) {
        if (!noticia.urlToImage.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(noticia.urlToImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen de la noticia",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = noticia.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = noticia.descripcion ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}