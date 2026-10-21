# App de asistencia del grupo

Comprueba quién está trabajando en el proyecto, cuenta las horas de cada uno e incluye un chat del grupo.
Funciona solo con GitHub: no hace falta ningún servidor.

## Qué hace

- **Al abrirla empieza tu sesión de trabajo.** Tu estado pasa a «Trabajando», tanto en la app como en tu perfil de GitHub (🟢).
- **Cada 25 minutos** aparece una ventana encima de todo: **«¿Sigues trabajando?»**.
  - Si respondes, sigue contando.
  - Si en **5 minutos** no respondes, te marca como **inactivo**: tu perfil de GitHub pasa a 💤 y la sesión se guarda **hasta tu última confirmación** (el tiempo sin confirmar no cuenta).
- **Pestaña Equipo:** quién está trabajando ahora y las horas de cada uno (hoy, esta semana y en total).
- **Pestaña Chat:** chat del grupo. Si mencionas a alguien con `@usuario`, GitHub le avisa por correo.
- Cerrar la ventana no cierra la app: se queda junto al reloj. Para salir, haz clic derecho en el icono → **Salir**, y la sesión se termina.
- **Al abrirla, actualiza tu carpeta con lo que hay en GitHub.** Antes hace una **copia de seguridad** completa en la carpeta oculta `IngasPutoAmo/` (guarda las 10 últimas). Solo actualiza si es seguro (`git pull --ff-only`). Si tus cambios chocan con los de GitHub, no toca nada y te avisa.
- **Al cerrar la app o al quedar inactivo**, tu trabajo se guarda en GitHub, en la rama **`copias/<usuario>`**. Se guarda todo, incluso lo que no tiene commit. No toca tu rama ni tus archivos.
  Para recuperar algo: `git fetch` y luego `git checkout origin/copias/<usuario> -- ruta/del/archivo`.

## Instalación (una vez)

1. Necesitas **Java 17 o superior** (el JDK de clase vale).
2. Crea un **token de GitHub (clásico)**: GitHub → *Settings* → *Developer settings* → *Personal access tokens* → *Tokens (classic)* → *Generate new token*.
   Marca los permisos **`repo`** y **`user`**. El permiso `user` sirve para cambiar tu estado de GitHub; sin él la app funciona igual, pero no toca tu perfil.
3. Haz doble clic en **`iniciar.bat`** (en Windows) o ejecuta `./iniciar.sh` (en Mac/Linux). La primera vez te pedirá el token.

El token se guarda **solo en tu PC**, en `~/.discoteca-asistencia/config.properties`. **Nunca lo subas al repo.**

> Además, tienes que ser **colaborador** del repositorio, si no GitHub no te deja escribir.

## Dónde se guardan los datos

| Qué | Dónde |
|---|---|
| Estado de cada persona | rama `asistencia-datos` → `presencia/<usuario>.json` |
| Horas trabajadas | rama `asistencia-datos` → `registro/<usuario>.csv` (`inicio,fin,minutos,motivo`) |
| Chat | Issue «💬 Chat del grupo» (etiqueta `chat-asistencia`) |
| Copia local de tus sesiones | `~/.discoteca-asistencia/registro-local.csv` |
| Tu trabajo al cerrar o quedar inactivo | rama `copias/<usuario>` |
| Copias de seguridad de tu carpeta | `IngasPutoAmo/` (oculta, solo en tu PC, no se sube) |

Usa una rama aparte para que `main` no se llene de commits. Si te quedas sin internet, las sesiones se guardan en tu PC y se suben en cuanto vuelve la conexión. Si el PC se apaga de golpe, la sesión se registra la próxima vez que abras la app.

## Ajustes opcionales

Están en `~/.discoteca-asistencia/config.properties`:

```properties
intervalo.minutos=25   # cada cuánto pregunta
gracia.minutos=5       # tiempo para responder
```

Para probar rápido puedes poner `intervalo.minutos=0.5` y `gracia.minutos=0.25`.

### Integración con JARVIS (solo para quien lo tenga)

Si añades lo siguiente, la app te avisa a través de JARVIS (en el móvil o el HUD) cuando alguien te menciona en el chat o cuando te marcan como inactivo:

```properties
jarvis.url=https://tu-jarvis
jarvis.token=<token de dispositivo de JARVIS>
```

Si estas líneas no están, la app no hace nada con JARVIS.
