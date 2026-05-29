# Solo Leveling Fitness - Android App

## 🎮 Descripción

App de fitness gamificada estilo **Solo Leveling / One Punch Man**. Entrena diario, sube de nivel, compite con amigos.

## ⚔️ Características

- **Misiones Diarias**: Flexiones, sentadillas, dominadas, abdominales, plancha, burpees, carrera...
- **Sistema de Niveles**: De E-Rank a Shadow Monarch (100+ niveles)
- **Multiplicador por Constancia**: Más días seguidos = más XP (hasta 5x)
- **Penalización por Inactividad**: Pierdes racha y XP si no entrenas
- **Estadísticas**: Fuerza, Velocidad, Resistencia, Aguante, Flexibilidad
- **Ranking Global**: Compite con todos los usuarios
- **Sistema de Amigos**: Añade amigos por código, ránking entre amigos
- **Logros**: 18+ logros desbloqueables
- **Tema Oscuro**: Estética Solo Leveling con púrpura/cian

## 🛠️ Tech Stack

- **Kotlin** + **Jetpack Compose** (UI)
- **Material 3** Design System
- **Room** (base de datos local)
- **Firebase Firestore** (sync, ranking, amigos)
- **DataStore** (preferencias locales)
- **Navigation Compose** (navegación)

## 🚀 Configuración

1. Crear proyecto en [Firebase Console](https://console.firebase.google.com)
2. Añadir app Android con package `com.sololeveling.fitness`
3. Descargar `google-services.json` y colocarlo en `app/`
4. Habilitar **Firestore Database** y **Authentication** en Firebase
5. Compilar con Android Studio o `./gradlew assembleDebug`

## 📁 Estructura

```
app/src/main/
├── java/com/sololeveling/fitness/
│   ├── data/
│   │   ├── model/          # Models (Mission, UserProfile, Stats...)
│   │   └── repository/     # GameRepository (Room + Firestore)
│   ├── ui/
│   │   ├── screens/        # Home, Ranking, Profile, Friends, MissionDetail
│   │   ├── theme/          # SoloLevelingTheme (dark theme)
│   │   └── components/     # Componentes reutilizables
│   ├── viewmodel/          # GameViewModel (estado central)
│   ├── util/               # GameEngine (lógica de XP/niveles)
│   ├── MainActivity.kt     # Navegación principal
│   └── SoloLevelingApp.kt  # Application class
└── res/                    # Strings, colors, themes
```

## 🎯 Sistema de Juego

### XP por nivel
```
XP_nivel = nivel² × 50 + nivel × 100
```

### Multiplicador de racha
| Racha (días) | Multiplicador |
|:---:|:---:|
| 0 | 1.0x |
| 3 | 1.3x |
| 7 | 1.9x |
| 14 | 2.6x |
| 30 | 3.0x |
| 30+ | hasta 5.0x (cap) |

### Penalización por inactividad
| Días sin entrenar | Efecto |
|:---:|:---:|
| 1 | Aviso (sin penalización) |
| 2 | -10% XP (usa 1 día de gracia) |
| 3 | -25% XP |
| 4+ | -50% XP + pierdes racha |

### Stats por ejercicio
| Ejercicio | Stat principal |
|:---:|:---:|
| Flexiones | Fuerza |
| Sentadillas | Fuerza |
| Dominadas | Fuerza |
| Abdominales | Aguante |
| Plancha | Aguante |
| Burpees | Resistencia |
| Carrera | Resistencia |
| Escaladores | Velocidad |
| Estiramientos | Flexibilidad |

## 📜 Licencia
MIT
