# Gestor Personal de Tareas

Aplicación nativa para Android desarrollada en **Kotlin** con **Jetpack Compose** y **Material 3**. Su objetivo es organizar tareas personales, académicas o profesionales de forma local, permitiendo filtrarlas, compartirlas con otras apps y consultar tareas de ejemplo desde un servicio web REST.

Para estructurar el proyecto se ha seguido el patrón **MVVM (Model-View-ViewModel)** con flujo de datos unidireccional (UDF), separando claramente la interfaz de usuario, la lógica de presentación, el modelo de datos, el almacenamiento local, las preferencias y la capa de red.

## Funcionalidades Implementadas

* **Gestión CRUD completa de tareas locales:**

  * **Crear:** Mediante un diálogo modal adaptativo (`DialogoTarea` con soporte de desplazamiento vertical) con campos para el título (obligatorio), descripción (opcional) y selector de prioridad desplegable (`ExposedDropdownMenuBox` con opciones `BAJA`, `MEDIA` y `ALTA`). Incluye validación para evitar títulos vacíos o de menos de 3 caracteres (tras recortar espacios) y bloqueo de doble pulsación en el botón *Guardar*.

  * **Listar:** Lista desplazable con `LazyColumn` usando claves únicas (`key = { tarea -> tarea.id }`) y un mensaje informativo en pantalla cuando no hay tareas que mostrar.

  * **Editar:** Botón de edición en cada tarjeta que abre el formulario precargado con los datos actuales de la tarea para actualizar su título, descripción o prioridad en la base de datos.

  * **Completar / Reactivar:** `Checkbox` interactivo en cada tarea que actualiza su estado en Room y aplica un tachado visual (`TextDecoration.LineThrough`) sobre el título cuando está finalizada.

  * **Eliminar con confirmación:** Al pulsar el icono de la papelera se muestra un `AlertDialog` pidiendo confirmación antes de borrar el registro definitivamente. Si se cancela, la tarea se mantiene intacta.

* **Almacenamiento permanente con Room:** Base de datos SQLite local (`tareas-db`) implementada con el patrón **Singleton** (`@Volatile` y `synchronized`) y compilada mediante **KSP**, con operaciones `@Query`, `@Insert`, `@Update` y `@Delete`.

* **Filtro persistente con Preferences DataStore:** Interruptor (`Switch`) *"Mostrar solo pendientes"* en la pantalla principal. Se combina el flujo de Room y el de DataStore mediante `combine` para filtrar las tareas en tiempo real, conservando el estado del interruptor al cerrar y volver a abrir la app.

* **Consulta de tareas externas (API REST):** Apartado independiente (*"Tareas de ejemplo"*) que consulta el endpoint `GET https://jsonplaceholder.typicode.com/todos?userId=1` utilizando **Retrofit** y **Gson**, manteniendo los datos remotos separados de las tareas personales sin guardarlos en Room.

* **Gestión de estados de red:** Uso de una jerarquía sellada (`EstadoApi`: `Inactivo`, `Cargando`, `Exito`, `Error`) para deshabilitar el botón de descarga mientras dura la petición, mostrar un `CircularProgressIndicator` y ofrecer un botón de reintento si falla la conexión.

* **Compartir tareas (`Intent` implícito):** Botón en cada tarjeta que prepara un texto con el título, la descripción y la prioridad de la tarea y lanza `Intent.ACTION_SEND` mediante `Intent.createChooser`, capturando `ActivityNotFoundException` por si el dispositivo no tiene aplicaciones compatibles instaladas.

* **Soporte para rotación y navegación del sistema:** Conservación del estado de navegación y formularios ante cambios de configuración mediante `rememberSaveable` y gestión del botón físico/gesto *Atrás* mediante `BackHandler`.

## Organización del Proyecto

El código fuente bajo el paquete `com.AzoguePablo.gestorpersonaldetareas` está dividido por responsabilidades:

```
com.AzoguePablo.gestorpersonaldetareas/
├── MainActivity.kt               # Punto de entrada, instanciación de ViewModels y navegación por estados
├── model/
│   └── Tarea.kt                  # Entidad @Entity ("tareas") y enum class Prioridad
├── data/
│   ├── TareaDao.kt               # Interfaz @Dao con consultas Flow y operaciones suspend CRUD
│   └── TareaDatabase.kt          # Clase abstracta @Database con patrón Singleton
├── preferences/
│   └── PreferenciasUsuario.kt    # Lectura y escritura en Preferences DataStore para el filtro
├── network/
│   ├── ClienteRetrofit.kt        # Interfaz ServicioTareasApi (@GET) y objeto Singleton ClienteRetrofit
│   └── TareaApi.kt               # Modelo de datos TareaApi mapeado con @SerializedName
├── viewmodel/
│   ├── TareaViewModel.kt         # Estado local (combine Room + DataStore) y TareaViewModelFactory
│   └── TareasApiViewModel.kt     # Gestión de estados de red (StateFlow<EstadoApi>) con coroutines
└── ui/
    ├── PantallaTareas.kt         # Pantalla principal, Switch de filtro, ItemTarea, compartirTarea y DialogoTarea
    ├── PantallaApi.kt            # Pantalla de consulta remota y renderizado según estado de red
    └── theme/                    # Tema, paleta de colores y tipografía Material 3

```

## Bibliotecas Utilizadas

* **Jetpack Compose & Material 3:** Desarrollo declarativo de la interfaz (`Scaffold`, `TopAppBar`, `LazyColumn`, `Card`, `AlertDialog`, `ExposedDropdownMenuBox`, `Switch`).

* **Lifecycle & ViewModel Compose:** `lifecycle-runtime-compose` y `viewModelScope` para gestionar el ciclo de vida y recolectar flujos de forma segura (`collectAsStateWithLifecycle`).

* **Room (`2.7.1`) + KSP:** `room-runtime`, `room-ktx` y `room-compiler` para persistencia SQLite local y soporte nativo de `Flow` y coroutines.

* **Jetpack DataStore (`1.1.1`):** `datastore-preferences` para guardar de forma asíncrona la preferencia del filtro de visualización.

* **Retrofit (`2.9.0`) & Gson (`2.9.0`):** `retrofit` y `converter-gson` para consumir el servicio web REST y deserializar las respuestas JSON.

* **Kotlin Coroutines & Flow:** Ejecución de operaciones de red y base de datos en segundo plano sin bloquear el hilo principal (`StateFlow`, `MutableStateFlow`, `combine`, `stateIn`).

## Instrucciones para Ejecutar la Aplicación

1. Clonar o descargar este repositorio en el equipo local.

2. Abrir la carpeta raíz del proyecto con **Android Studio**.

3. Esperar a que **Gradle** sincronice y descargue las dependencias necesarias (o pulsar **File > Sync Project with Gradle Files**).

4. Seleccionar un emulador Android (AVD) con **API 24 (Android 7.0) o superior**, o conectar un dispositivo físico con depuración USB activada.

5. Pulsar **Run** (`Shift + F10`) para compilar e instalar la aplicación, o bien instalar directamente el archivo `app-debug.apk` incluido en el repositorio.

## Configuraciones de los Emuladores Utilizados

Para comprobar la adaptación de la interfaz y el funcionamiento general se han realizado las pruebas en dos perfiles distintos de emulador en Android Studio:

| Parámetro | Emulador 1 (Pantalla grande / Reciente) | Emulador 2 (Pantalla compacta / Compatibilidad) | 
| ----- | ----- | ----- | 
| **Dispositivo** | Pixel 8 | Nexus 5X / Pixel 4a | 
| **Versión de Android** | Android 14 / 15 (API 34 - 36) | Android 7.0 (API 24 — `minSdk`) | 
| **Resolución de pantalla** | 1080 x 2400 px (420 dpi) | 1080 x 1920 px (420 dpi) | 
| **Arquitectura CPU** | x86_64 | x86_64 | 
| **RAM / Almacenamiento** | 2048 MB RAM / 6 GB Internal Storage | 1536 MB RAM / 4 GB Internal Storage | 

## Tabla de Pruebas Realizadas y Resultados Obtenidos

| Nº | Prueba | Pasos Ejecutados | Resultado Esperado | Resultado Obtenido | Estado | 
| ----- | ----- | ----- | ----- | ----- | ----- | 
| **1** | **Crear una tarea válida** | Pulsar `+`, rellenar título válido ($\ge 3$ caracteres), descripción y prioridad, y pulsar *Guardar*. | Se guarda en Room y aparece en la lista principal. | Tarea persistida y mostrada al instante arriba del listado. | Superada | 
| **2** | **Intentar guardar un título vacío** | Pulsar `+`, dejar el título vacío, solo con espacios o con menos de 3 caracteres y pulsar *Guardar*. | Se muestra un error comprensible y no se crea la tarea. | Mensaje de validación visible junto al campo; no se inserta registro. | Superada | 
| **3** | **Editar una tarea** | Pulsar el icono de edición en una tarea, modificar título, descripción o prioridad y pulsar *Guardar*. | Los cambios se muestran correctamente y se actualizan en Room. | Datos actualizados en pantalla y guardados en SQLite. | Superada | 
| **4** | **Marcar una tarea como completada** | Pulsar el `Checkbox` de una tarea pendiente. | Su estado cambia a completada, se tacha el título y queda guardado. | Estado actualizado en Room y tachado aplicado en la UI. | Superada | 
| **5** | **Cancelar una eliminación** | Pulsar el icono de eliminar y elegir *Cancelar* en el `AlertDialog`. | La tarea no desaparece y permanece sin cambios. | Diálogo cerrado manteniendo la tarea intacta. | Superada | 
| **6** | **Eliminar una tarea confirmando** | Pulsar el icono de eliminar y confirmar pulsando *Eliminar* en el `AlertDialog`. | La tarea desaparece de la base de datos y del listado. | Eliminación definitiva en SQLite y actualización inmediata de la lista. | Superada | 
| **7** | **Cerrar y volver a abrir la aplicación** | Crear y modificar tareas, cerrar la app completamente desde recientes y abrirla de nuevo. | Se recuperan automáticamente las tareas guardadas en Room. | Todas las tareas se restauran sin pérdida de datos. | Superada | 
| **8** | **Activar el filtro y reiniciar** | Activar el `Switch` *"Mostrar solo pendientes"*, cerrar la app por completo y reiniciar. | Se conserva la preferencia seleccionada mediante DataStore. | El `Switch` inicia activado y filtra las tareas completadas. | Superada | 
| **9** | **Consultar la API con Internet** | Entrar en *"Tareas de ejemplo"* y pulsar *"Descargar tareas del servidor"* con conexión activa. | Se deshabilita el botón durante la carga y se muestran las tareas recibidas. | Indicador de carga mostrado y listado de `jsonplaceholder` renderizado. | Superada | 
| **10** | **Consultar la API sin conexión** | Desactivar WiFi/datos en el emulador e intentar descargar las tareas desde la API. | Se muestra un mensaje de error claro y se permite reintentar. | Error controlado sin bloqueo ni cierre, con botón de reintento operativo. | Superada | 
| **11** | **Compartir una tarea** | Pulsar el icono de compartir en una tarea personal. | Se abre el selector de aplicaciones compatibles (`ACTION_SEND`) con el texto preparado. | Selector nativo desplegado con título, descripción y prioridad. | Superada | 
| **12** | **Cambiar la orientación del dispositivo** | Girar el emulador a horizontal teniendo tareas cargadas, el filtro activo o diálogos abiertos. | La aplicación sigue funcionando y conserva sus datos y estado actual. | Interfaz adaptada manteniendo el estado de los `ViewModel`, navegación y Room. | Superada | 

## Capturas de Pantalla

* **Listado principal y filtro activo:**

* **Formulario de creación / edición de tarea y validación:**

* **Diálogo de confirmación de borrado:**

* **Consulta de tareas de ejemplo (API REST):**

## Enlace al Chat de IA Usado Durante el Desarrollo

* **Asistente utilizado:** Google Gemini

* **Enlace a la conversación:**  https://gemini.google.com/share/d/1MZ4Wx1ihzUJxSTY6isWmTIWggDM8d6pD?usp=sharing

## Incidencias Conocidas y Decisiones Técnicas

* **Migración de `kapt` a KSP para Room:** Durante la configuración inicial en Windows surgió un error de compilación en las tareas de `kapt` (`NativeLibraryNotFoundException` de SQLite al verificar la base de datos con Kotlin 2.2). Se solucionó migrando el procesador de anotaciones de Room a **KSP (`com.google.devtools.ksp`)** con Room `2.7.1`.

* **Navegación por estados en Compose:** Para mantener la estructura sencilla y ligera sin añadir dependencias externas de navegación, se implementó el cambio entre la pantalla de tareas locales y la pantalla de consulta a la API mediante una variable de estado persistente (`rememberSaveable`) y `BackHandler` en `MainActivity`.
