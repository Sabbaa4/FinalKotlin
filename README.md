# 🎮 Gameboxd — რეტრო თამაშების ბექლოგ ტრეკერი

## 📋 აპლიკაციის აღწერა

**Gameboxd** არის Android აპლიკაცია, რომელიც მომხმარებელს საშუალებას აძლევს მართოს თამაშების ბექლოგი. აპლიკაცია რეტრო/8-bit ესთეტიკითაა შექმნილი და გთავაზობთ:

- 🎮 **თამაშების დამატება/რედაქტირება/წაშლა** — სრული CRUD ოპერაციები
- 📊 **სტატუსის მართვა** — Backlog, Playing, Finished
- ⭐ **1-5 ვარსკვლავით შეფასება** — ინტერაქტიული რეიტინგის სისტემა
- 🔀 **Drag & Drop პრიორიტიზაცია** — ხელით გადალაგება drag handle-ით
- 🔍 **სტატუსით ფილტრაცია** — Toolbar მენიუდან სწრაფი ფილტრაცია
- ↩️ **Swipe-to-Delete** — მარცხნივ გადაფურცვლა წაშლით და Undo შესაძლებლობით

---

## 🏗️ ტექნიკური არქიტექტურა

### MVVM (Model-View-ViewModel)

აპლიკაცია აგებულია **MVVM** არქიტექტურული პატერნით, რომელიც უზრუნველყოფს მონაცემების და UI ლოგიკის გამიჯვნას:

```
┌─────────────────────────────────────────────────────┐
│                      View                           │
│  (MainActivity, GameAdapter, AddGameDialogFragment) │
│         ↕ LiveData observation                      │
├─────────────────────────────────────────────────────┤
│                   ViewModel                         │
│              (GameViewModel)                        │
│         ↕ Repository Pattern                        │
├─────────────────────────────────────────────────────┤
│                    Model                            │
│     (GameRepository → GameDao → Room Database)      │
└─────────────────────────────────────────────────────┘
```

- **View** — პასუხისმგებელია მხოლოდ UI-ს ჩვენებაზე. `LiveData`-ს observe-ით ავტომატურად რეაგირებს მონაცემების ცვლილებაზე
- **ViewModel** — ინახავს UI-სთვის საჭირო მონაცემებს და გადარჩენილია configuration change-ებისას (მაგ.: ეკრანის როტაცია)
- **Model** — Room Database და Repository, რომელიც მართავს მონაცემთა ბაზასთან კომუნიკაციას

### Room Database

Room არის Android-ის ოფიციალური ORM ბიბლიოთეკა SQLite-ის თავზე:

| კომპონენტი | კლასი | აღწერა |
|------------|-------|--------|
| **Entity** | `Game` | მონაცემთა ბაზის ცხრილის სტრუქტურა — `id`, `title`, `platform`, `status`, `rating`, `sortOrder` |
| **DAO** | `GameDao` | Data Access Object — CRUD ოპერაციები (`@Insert`, `@Update`, `@Delete`, `@Query`) |
| **Database** | `GameDatabase` | `RoomDatabase`-ის ქვეკლასი, Singleton პატერნით — ერთი ინსტანცია მთელი აპლიკაციისთვის |

**Game Entity სტრუქტურა:**

```kotlin
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,        // თამაშის სახელი
    val platform: String,     // პლატფორმა (NES, SNES, PS1...)
    val status: String,       // სტატუსი (Backlog/Playing/Finished)
    val rating: Int,          // რეიტინგი (1-5)
    val sortOrder: Int = 0    // Drag & Drop თანმიმდევრობა
)
```

**Reactive Queries** — DAO აბრუნებს `Flow<List<Game>>`-ს, რაც უზრუნველყოფს მონაცემების ცვლილებისას ავტომატურ განახლებას UI-ში LiveData-ს მეშვეობით.

### ViewBinding

პროექტში View-ებზე წვდომა ხდება ექსკლუზიურად **ViewBinding**-ით:

- ✅ **Compile-time type safety** — კომპილაციისას ხდება ტიპის შემოწმება
- ✅ **Null safety** — არ არსებობს `NullPointerException`-ის რისკი
- ✅ **ყოველი XML layout ავტომატურად გენერირებს Binding კლასს:**

| XML Layout | Binding კლასი | გამოყენება |
|-----------|---------------|-----------|
| `activity_main.xml` | `ActivityMainBinding` | `MainActivity` |
| `item_game.xml` | `ItemGameBinding` | `GameAdapter.GameViewHolder` |
| `dialog_add_game.xml` | `DialogAddGameBinding` | `AddGameDialogFragment` |

**გამოყენების მაგალითი:**

```kotlin
// Activity-ში
binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)

// Adapter-ში
val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)

// DialogFragment-ში
_binding = DialogAddGameBinding.inflate(layoutInflater)
```

### Repository Pattern

`GameRepository` არის ერთადერთი წყარო (Single Source of Truth) მონაცემებისთვის:

```
ViewModel → GameRepository → GameDao → Room Database (SQLite)
```

- Repository ახდენს DAO-ს მეთოდების ინკაფსულაციას
- ViewModel არ ურთიერთობს პირდაპირ DAO-სთან
- მომავალში შესაძლებელია ქსელური მონაცემების წყაროს დამატებაც Repository-ს დონეზე

---

## 🎯 ნოველ ფიჩერი — Drag & Drop (ItemTouchHelper)

აპლიკაციის მთავარი ინოვაციური ფიჩერია **Drag & Drop** ფუნქციონალი, რომელიც მომხმარებელს საშუალებას აძლევს ხელით გადაალაგოს თამაშები პრიორიტეტის მიხედვით.

### როგორ მუშაობს:

```
┌──────────────────────────────────────────────────┐
│  1. მომხმარებელი ეხება Drag Handle (≡) ხატულას  │
│                    ↓                             │
│  2. ItemTouchHelper იჭერს ACTION_DOWN event-ს   │
│                    ↓                             │
│  3. onMove() — Adapter-ში პოზიციები იცვლება     │
│                    ↓                             │
│  4. clearView() — გადათრევა დასრულდა            │
│                    ↓                             │
│  5. ViewModel.updateSortOrders() — ახალი         │
│     თანმიმდევრობა ინახება Room Database-ში       │
└──────────────────────────────────────────────────┘
```

### ტექნიკური დეტალები:

1. **`ItemTouchHelper.SimpleCallback`** — მართავს UP/DOWN ჟესტებს (Drag) და LEFT ჟესტს (Swipe)
2. **Drag Handle** — `imageViewDragHandle` (≡ ხატულა) — მომხმარებელი ამ ხატულაზე შეხებით იწყებს გადათრევას
3. **`onMove()`** — ყოველი გადაადგილებისას `adapter.moveItem(from, to)` ცვლის ელემენტების პოზიციებს `ListAdapter`-ის სიაში
4. **`clearView()`** — გადათრევის დასრულებისას ხდება ახალი თანმიმდევრობის აღება და `ViewModel`-ში გადაცემა, რომელიც `sortOrder` ველებს განაახლებს Room Database-ში
5. **`sortOrder`** ველი `Game` Entity-ში — უზრუნველყოფს თანმიმდევრობის შენახვას აპლიკაციის გადატვირთვის შემდეგაც
6. **Swipe-to-Delete** — მარცხნივ გადაფურცვლა წაშლის თამაშს, Snackbar-ით **Undo** შესაძლებლობით

### კლასი `GameTouchHelper`:

```kotlin
class GameTouchHelper(
    private val adapter: GameAdapter,
    private val onMoveComplete: (List<Game>) -> Unit,
    private val onSwipedToDelete: (Game) -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,  // Drag მიმართულებები
    ItemTouchHelper.LEFT                          // Swipe მიმართულება
)
```

---

## 📁 პროექტის სტრუქტურა

```
com.example.finalproject/
│
├── data/                          # მონაცემთა ფენა
│   ├── Game.kt                    # Entity — ცხრილის სტრუქტურა
│   ├── GameDao.kt                 # DAO — მონაცემთა ბაზის ოპერაციები
│   └── GameDatabase.kt            # Room Database — Singleton
│
├── repository/                    # Repository ფენა
│   └── GameRepository.kt         # მონაცემების ერთიანი წყარო
│
├── viewmodel/                     # ViewModel ფენა
│   ├── GameViewModel.kt           # UI-ს მონაცემები და ბიზნეს ლოგიკა
│   └── GameViewModelFactory.kt    # ViewModel-ის Factory
│
├── adapter/                       # RecyclerView კომპონენტები
│   ├── GameAdapter.kt             # ListAdapter + DiffUtil + ViewBinding
│   └── GameTouchHelper.kt        # Drag & Drop + Swipe-to-Delete
│
├── ui/                            # UI კომპონენტები
│   └── AddGameDialogFragment.kt   # დამატება/რედაქტირების დიალოგი
│
└── MainActivity.kt                # მთავარი Activity
```

---

## 🛠️ გამოყენებული ტექნოლოგიები

| ტექნოლოგია | ვერსია/ტიპი | გამოყენება |
|------------|------------|-----------|
| **Kotlin** | 1.9+ | პროგრამირების ენა |
| **Room** | AndroidX | ლოკალური მონაცემთა ბაზა (SQLite ORM) |
| **ViewBinding** | AndroidX | Type-safe View წვდომა (კომპილაციისას გენერირებული binding კლასებით) |
| **LiveData** | AndroidX Lifecycle | რეაქტიული მონაცემები — UI ავტომატურად განახლდება |
| **ViewModel** | AndroidX Lifecycle | UI მონაცემების მართვა, lifecycle-aware |
| **RecyclerView** | AndroidX | ეფექტური სიების ჩვენება |
| **ListAdapter** | AndroidX | RecyclerView Adapter DiffUtil-ით |
| **DiffUtil** | AndroidX | ეფექტური სიის განახლება (მხოლოდ შეცვლილი ელემენტები) |
| **ItemTouchHelper** | AndroidX | Drag & Drop და Swipe ჟესტები |
| **Material Components** | Google | FAB, Snackbar, Toolbar, CardView |
| **Coroutines** | Kotlin | ასინქრონული ოპერაციები (Room queries) |

---

## 🚀 პროექტის გაშვება

### წინაპირობები

- **Android Studio** Hedgehog ან უფრო ახალი
- **JDK** 17+
- **Android SDK** API 24+ (მინიმალური)
- Android Emulator ან ფიზიკური მოწყობილობა

### ინსტალაცია

```bash
# 1. რეპოზიტორიის კლონირება
git clone https://github.com/your-username/gameboxd.git

# 2. გახსენით Android Studio-ში
# File → Open → აირჩიეთ პროექტის ფოლდერი

# 3. Gradle Sync
# Android Studio ავტომატურად შემოგთავაზებთ Sync-ს,
# ან: File → Sync Project with Gradle Files

# 4. გაშვება
# Run → Run 'app' (Shift+F10)
# აირჩიეთ Emulator ან დაკავშირებული მოწყობილობა
```

### Build

```bash
# Debug APK-ის აგება
./gradlew assembleDebug

# APK მდებარეობა:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📸 ფუნქციონალი

| ფუნქცია | აღწერა |
|---------|--------|
| ➕ **დამატება** | FAB ღილაკით — სათაური, პლატფორმა, სტატუსი, რეიტინგი |
| ✏️ **რედაქტირება** | თამაშის ბარათზე დაჭერით — იხსნება რედაქტირების დიალოგი |
| 🗑️ **წაშლა** | მარცხნივ Swipe ან რედაქტირების დიალოგიდან Delete ღილაკი |
| ↩️ **Undo** | წაშლის შემდეგ Snackbar-ით „დაბრუნება" ოპცია |
| 🔀 **გადალაგება** | Drag Handle (≡) ხატულაზე დაჭერით — Drag & Drop |
| 🔍 **ფილტრაცია** | Toolbar მენიუდან — All / Backlog / Playing / Finished |
| ⭐ **რეიტინგი** | 1-5 ვარსკვლავის ინტერაქტიული არჩევა |

---

## 👨‍💻 ავტორი

შექმნილია Android Development კურსის ფინალური პროექტისთვის.

**ტექნოლოგიური სტეკი:** Kotlin · MVVM · Room · ViewBinding · Material Design

---

> 🕹️ *"Every game deserves to be tracked, every backlog deserves to be conquered."*
