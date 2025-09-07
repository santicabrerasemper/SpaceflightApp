SpaceFlightApp

A simple news app that lists and shows details of spaceflight articles with search, pagination, pull-to-refresh, and friendly error/empty states.

    //Tech Stack -->

*Kotlin, Coroutines & Flow

*MVVM (+ light Clean layers)

*DI: Koin

*Networking: Retrofit + OkHttp

*Images: Coil

*UI: Material 3, ConstraintLayout, SwipeRefreshLayout

*Navigation: Jetpack Navigation


    //Features -->

    Article list:

*Reactive search (debounce + distinctUntilChanged), local ranking

*Manual endless scroll pagination

*Pull-to-refresh:

*If list has items → refresh spinner in SwipeRefreshLayout

*If list is empty → full-screen loading

*Empty state card with “Clear search”

*Error state card (icon + title + optional message + Retry)

    Detail screen:

*Hero image, title, site, nicely formatted date/timezone

*Featured chip, authors (ChipGroup), Open/Share actions

*Same error overlay + Retry pattern

//Architecture -->

    Layers:

*data/ (Retrofit API, DTOs, mappers, repositories)

*domain/ (models, use cases)

*ui/ (fragments, viewmodels, adapters, layouts)

*core/utils/ (UiError, DateFmt, relevanceScore, etc.)

    MVVM + StateFlow:

*List screen exposes ArticlesUiState: items, isLoading, isRefreshing, isPaging, error, query, offset, hasLoadedOnce, endReached

*Detail screen exposes ArticleDetailUiState: Loading / Error(UiError) / Content(article, isRefreshing)

*Fragments only render state



    Why Swipe-to-Refresh?

*It’s the standard Android affordance for manual refresh; simple, predictable, and pairs well with pagination.

    Dependency Injection

*Koin for concise modules and easy testing/mocking.

    DTOs (and no Gson)

*DTOs are mapped to domain models to decouple UI from network. Prefer kotlinx.serialization or Moshi over Gson for better Kotlin support and less reflection.

    Navigation

*Jetpack Navigation with a simple graph: List → Detail (passing articleId).

    ViewModel Persistence

*SavedStateHandle persists query/offset across process death and config changes.

    Error Handling

*UiError sealed class:

*Network, Server(code), WithMessage(msg), Unknown

*Extensions: iconRes(), titleRes(), message(ctx)

*List/Detail render a full-screen card with Retry. Retry shows full-screen loader when the list is empty; otherwise uses the swipe spinner.

    Dates & Timezones

*DateFmt formats ISO strings for:

*Cards: compact (e.g., Mon, 4 Mar • 10:15 (GMT+1))

    Keyboard & Back UX

*Tapping outside search hides IME.

*Smart back with query:

*If there’s a query and IME is visible → first back only hides IME

*If there’s a query and IME is hidden → back clears the query (exits empty state)

*If no query → normal system back

    Run

Open in Android Studio (Giraffe/Koala+)

Build & Run (no keys needed)



    Project Layout (short)
app/
├─ core/
├─ utils/
└─ extensions/
├─ helpers/ 

├─ constants/

├─ data/ (api, dto, mapper, repository)
├─ domain/ (model, usecase)
└─ ui/
├─ list/   (Fragment, ViewModel, Adapter)
└─ detail/ (Fragment, ViewModel)

    Future Work

*Backwards compatibility with gestures

*Paging 3

*UI Improvements : Typography, animations, better colors pallet

*Unit/UI tests + CI

*Migration to Compose