# BreakEvenLite 📊

**BreakEvenLite** is a fast, intuitive, and 100% offline Android utility app designed for business owners, entrepreneurs, managers, students, and financial analysts to perform instant break-even analyses, evaluate margin contributions, simulate price changes, and plan target profits.

Built using modern Android development practices with **Jetpack Compose** and **Material 3**, BreakEvenLite provides a clean, responsive UI with zero tracking, zero ads, and no internet connection required.

---

## ✨ Features

- 🧮 **Comprehensive Break-Even Calculations**:
  - **Inputs**: Fixed Costs, Selling Price per Unit, and Variable Cost per Unit.
  - **Contribution Margin**: Automatically calculates margin per unit and contribution margin ratio (%).
  - **Break-Even Point**: Calculates exact decimal break-even units as well as practical ceiling-rounded units required to cover fixed costs.
  - **Break-Even Sales Revenue**: Computes total required sales volume in currency.
  - **Cost Structure Breakdown**: Visualizes Fixed Costs, Total Variable Costs, Total Costs, and Net Profit at break-even threshold.

- 🎯 **Target Profit Analysis**:
  - Input a desired net profit goal to determine required sales volume (units & revenue).
  - Highlights additional units required above the break-even baseline.

- 💡 **What-If Price Simulator**:
  - Interactively simulate different selling price points to see immediate effects on contribution margin and break-even thresholds.

- 📐 **Dynamic Formula Breakdown**:
  - Displays step-by-step mathematical formulas populated dynamically with user inputs for complete clarity.

- 📜 **Calculation History & Management**:
  - Save up to 50 calculation scenarios locally on device.
  - **Search**: Search saved scenarios by name or notes.
  - **Filter & Sort**: Filter by scenario status (All, Available, No Break-Even, Target Profit) and sort by Recently Updated, Name A–Z, Lowest Break-Even Units, or Highest Contribution Margin.
  - **Favorites**: Star favorite scenarios for quick prioritization.
  - **Actions**: Restore scenario inputs into calculator, duplicate/copy scenarios, or delete unwanted records.

- 🔒 **100% Offline & Private**:
  - Requires zero permissions (no network access, no account required, no analytics).
  - All history and preferences are saved locally on device using **Jetpack DataStore**.

- 🎨 **Modern Design & Customization**:
  - Built with **Material 3** guidelines supporting System, Light, and Dark themes.
  - **Dynamic Color**: Supports Android 12+ wallpaper color palette matching.
  - **Multi-Currency Support**: Choose default currency ($, €, £, ₹, ¥, ₱, AED, SAR, EGP, etc.).
  - **Custom Precision**: Configure decimal precision (0 to 4 decimal places).
  - **Toggleable Display Sections**: Customize visible cards on the calculator screen.

---

## 🛠 Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Domain Layer
- **Asynchronous & Reactive**: Kotlin Coroutines & `StateFlow` / `Flow`
- **Data Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) with custom JSON serialization
- **Precision Math**: Exact financial calculations using `BigDecimal` and `BigInteger` with explicit rounding strategies (`HALF_UP`, `CEILING`) to prevent floating-point inaccuracy
- **Unit Testing**: JUnit 4 unit tests covering domain calculator logic and data adapters

---

## 📂 Project Structure

```
com.example.breakevenlite/
├── data/
│   ├── local/              # PreferencesDataStore & RecordJsonAdapter
│   ├── model/              # BreakEvenInput, BreakEvenResult, CalculationRecord, AppSettings
│   └── repository/         # HistoryRepository & SettingsRepository
├── domain/
│   ├── calculator/         # BreakEvenCalculator & BreakEvenValidator (Pure Kotlin engine)
│   └── util/               # CurrencyFormatter
└── ui/
    ├── navigation/         # Navigation graph & Screen routes
    ├── screens/
    │   ├── about/          # About & Privacy Screen
    │   ├── calculator/     # Main Calculator Screen & cards (Input, Contribution, Formula, etc.)
    │   ├── history/        # History Screen & Saved Item cards
    │   ├── settings/       # App Settings & Preferences
    │   └── splash/         # Splash Screen
    ├── theme/              # Color, Type, and Material Theme definition
    └── viewmodel/          # BreakEvenViewModel & Factory
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Version 11 or higher
- **Android SDK**: Minimum API 24 (Android 7.0 Nougat), Target API 37

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/BreakEvenLite.git
   cd BreakEvenLite
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open** -> Navigate to the cloned `BreakEvenLite` directory.

3. **Sync Gradle**:
   Allow Android Studio to sync dependencies from Gradle.

4. **Run the App**:
   Select your connected device or emulator and press **Run** (`Shift + F10`).

---

## 🧪 Running Tests

To run the unit test suite for the calculation domain logic and data adapters:

```bash
./gradlew test
```

---

## 📄 License

```text
Copyright (C) 2025 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
