# Solo Leveling Fitness

<p align="center">
  <img src="https://raw.githubusercontent.com/ephemere24/solo-leveling-fitness/main/docs/logo.png" alt="Solo Leveling Fitness" width="300"/>
</p>

<p align="center">
  <b>Entrena como un cazador. Sube de nivel como en Solo Leveling.</b>
</p>

---

## Que es

**Solo Leveling Fitness** es una app de fitness gamificada para Android. Cada entrenamiento completa misiones diarias, sube tus stats (Fuerza, Velocidad, Resistencia, Flexibilidad, Aguante) y escala niveles — de E-Rank hasta Shadow Monarch.

Inspirado en **Solo Leveling** y **One Punch Man**. Sin excusas. Solo resultados.

---

## Descarga

| Version | APK |
|---------|-----|
| v2.3 (ultima) | [Descargar](https://github.com/ephemere24/solo-leveling-fitness/releases/download/v2.3/solo-leveling-fitness-v2.3.apk) |
| Todas las versiones | [Releases](https://github.com/ephemere24/solo-leveling-fitness/releases) |

**Instalacion:** Descarga el APK, habilita "Instalar desde origen desconocido" y abre. Al ser una APK debug necesitas desinstalar versiones anteriores antes de instalar.

---

## Caracteristicas

### Misiones Diarias
9 misiones fijas cada dia, siempre en el mismo orden. Cada una sube multiples stats:

| # | Mision | Stats que sube |
|---|--------|-----------------|
| 1 | Flexiones | +2 STR, +1 END |
| 2 | Abdominales | +1 STR, +1 END, +1 SPD |
| 3 | Sentadillas | +2 STR, +1 END, +1 FLEX |
| 4 | Dominadas | +3 STR, +1 END |
| 5 | Sprint 100m | +3 SPD |
| 6 | Carrera 3km | +1 SPD, +3 END |
| 7 | Zancadas | +1 STR, +1 END, +1 FLEX, +1 SPD |
| 8 | Estiramientos 3min | +3 FLEX |
| 9 | Saltar cuerda 5min | +1 SPD, +2 END, +2 STA |

### Sistema de Niveles
Mas de 100 niveles. La XP por nivel sigue una formula progresiva:

```
XP_nivel = nivel^2 * 50 + nivel * 100
```

Rangos: **E-Rank -> D-Rank -> C-Rank -> B-Rank -> A-Rank -> S-Rank -> Monarch**

### Multiplicador por Constancia
Entrena dias seguidos para multiplicar tu XP:

| Racha | Multiplicador |
|:-----:|:-------------:|
| 0 dias | 1.0x |
| 3 dias | 1.3x |
| 7 dias | 1.9x |
| 14 dias | 2.6x |
| 30 dias | 3.0x |
| 30+ dias | hasta 5.0x |

### Penalizacion por Inactividad
Si no entrenas, pierdes:

| Dias sin entrenar | Penalizacion |
|:-----------------:|:-------------:|
| 1 | Aviso (sin perdida) |
| 2 | -10% XP |
| 3 | -25% XP |
| 4+ | -50% XP y pierdes la racha |

### Estadisticas
5 stats principales que evolucionan con cada ejercicio:
- **STR** (Fuerza) — Dominadas, flexiones, sentadillas
- **SPD** (Velocidad) — Sprint, saltar cuerda
- **END** (Resistencia) — Carrera 3km, burpees
- **FLEX** (Flexibilidad) — Estiramientos, zancadas
- **STA** (Aguante) — Saltar cuerda

### Ranking Global
Compite con todos los usuarios de la app. Sincronizacion en tiempo real via Firebase.

### Sistema de Amigos
Anade amigos por codigo y compite en un ranking exclusivo entre vosotros.

### Auto-completar
Las misiones se marcan automaticamente como completas cuando alcanzas el objetivo. Sin friccion.

### UI Cyberpunk
Tema oscuro con acentos purpura/cian. 0 emojis — todo con Material Icons.

---

## Tech Stack

| Tecnologia | Uso |
|-----------|-----|
| Kotlin | Lenguaje principal |
| Jetpack Compose | UI declarativa |
| Material 3 | Design System |
| Firebase Auth | Autenticacion anonima |
| Firestore | Sync, ranking global, amigos |
| DataStore | Preferencias locales |
| Navigation Compose | Navegacion entre pantallas |
| Splash Screen API | Animacion de inicio |

---

## Configuracion (desarrollo)

1. Clonar el repo:
   ```bash
   git clone https://github.com/ephemere24/solo-leveling-fitness.git
   ```

2. Crear proyecto en [Firebase Console](https://console.firebase.google.com) con package `com.sololeveling.fitness`

3. Descargar `google-services.json` y colocarlo en `app/`

4. Habilitar **Firestore Database** y **Authentication** (Anonymous) en Firebase

5. Compilar:
   ```bash
   ./gradlew assembleDebug
   ```

6. APK generado en `app/build/outputs/apk/debug/app-debug.apk`

---

## Estructura del Proyecto

```
app/src/main/
├── java/com/sololeveling/fitness/
│   ├── data/
│   │   ├── model/          # Modelos (Mission, UserProfile, Stats, Achievement)
│   │   └── repository/     # GameRepository (Room + Firestore)
│   ├── ui/
│   │   ├── screens/        # HomeScreen, RankingScreen, ProfileScreen, MissionDetailScreen...
│   │   ├── theme/          # SoloLevelingTheme (dark theme morado/cian)
│   │   └── components/     # Barras de progreso, cards, dialogs
│   ├── viewmodel/          # GameViewModel (estado central)
│   ├── util/               # GameEngine (logica XP/niveles/misiones)
│   ├── MainActivity.kt     # Navegacion principal
│   └── SoloLevelingApp.kt  # Application class
└── res/                    # Strings, colors, iconos, themes
```

---

## Roadmap

- [ ] Notificaciones push para recordatorio de entrenamiento
- [ ] Modo claro / selector de tema
- [ ] Widget de misiones diarias
- [ ] Exportar estadisticas (PDF/CSV)
- [ ] Retos semanales y eventos especiales
- [ ] Version iOS (SwiftUI)

---

## Licencia

MIT
