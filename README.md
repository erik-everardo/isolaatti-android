# Isolaatti Android


Se trata de una aplicación para dispositivos Android que implementa características de red social, tales como publicaciones, comentarios, imagenes, likes, perfiles, y audios.

Se conecta con el backend https://isolaatti.com

## Estructura del código
* Se siguen principios de clean architecture para el desarrollo de esta app.
* Se utilizan views (XML)
* Se siguen utilizando fragments y activities
* Se utilizan contracts para interactuar con los Activities
* Se utiliza View Binding

## Bibliotecas
* Retrofit https://square.github.io/retrofit/
* Hilt https://dagger.dev/hilt/
* Coil https://coil-kt.github.io/coil/
* Room https://developer.android.com/jetpack/androidx/releases/room?hl=es-419
* TouchImageView https://github.com/MikeOrtiz/TouchImageView
* Markwon https://github.com/noties/Markwon
* Media 3 https://developer.android.com/jetpack/androidx/releases/media3?hl=es-419
* Algunos modulos de Android Jetpack y bibliotecas de compatibilidad (revisar build.gradle)

## Compilar
Para compilar la app es necesario utilizar Android Studio. Modifica el archivo local.properties para incluir las siguientes propiedades según tu implementación:
```
backend=https://isolaatti.com
clientId=
secret=
terms=https://isolaatti.com/terminos_de_uso
privacyPolicy=https://isolaatti.com/politica_de_privacidad
sourceCodeUrl=https://github.com/Isolaatti-Software/isolaatti-android
blogUrl=https://isolaattisoftware.com.mx/
openSourceLicences=https://files.isolaatti.com/licencias.html
```


## Características planeadas
* Grabar audios
* Squads o grupos
* Notificaciones push
* Pantalla de notificaciones
* Pantalla de búsqueda
* Borradores (planeado)
* Pantalla de ajustes (planeado)
* Pantalla de información de app (planeado)

## Características en progreso
* Perfil
* Comentarios (faltan agregar foto y audio)
* Reproducir audios
* Posts (depende de grabar audios)

## Características finalizadas (o finalizadas mejorables)
* Subir fotos
