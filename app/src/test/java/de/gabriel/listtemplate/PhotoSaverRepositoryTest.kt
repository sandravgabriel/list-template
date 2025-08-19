package de.gabriel.listtemplate

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider // Von androidx.test.core.ktx
import androidx.test.ext.junit.runners.AndroidJUnit4 // Wichtig für Robolectric mit JUnit 4
import de.gabriel.listtemplate.data.PhotoSaverRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.* // Für runTest, UnconfinedTestDispatcher etc.
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder // Für robustere temporäre Ordner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class PhotoSaverRepositoryTest {

    // StandardTestDispatcher ist oft besser für vorhersagbare Tests als UnconfinedTestDispatcher,
    // da er mehr Kontrolle über die Ausführungsreihenfolge gibt.
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: PhotoSaverRepository
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var context: Context

    // TemporaryFolder Rule ist eine gute Praxis für das Management von Testdateien und -ordnern.
    // Es stellt sicher, dass die Ordner nach jedem Test (oder nach der Testklasse) aufgeräumt werden.
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testCacheDirInTemp: File
    private lateinit var testFilesDirInTemp: File

    // Dummy URI und Daten für Tests
    private val testUri: Uri = Uri.parse("content://com.example.test/dummy")
    private val testPhotoData = "This is a test photo content."
    private val testPhotoBytes = testPhotoData.toByteArray()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Hauptdispatcher für Coroutines setzen

        context = ApplicationProvider.getApplicationContext()
        mockContentResolver = mockk()

        // Erstelle Unterverzeichnisse im TemporaryFolder, die die Struktur von cacheDir/filesDir simulieren
        testCacheDirInTemp = tempFolder.newFolder("cache")
        testFilesDirInTemp = tempFolder.newFolder("files")

        // Erstelle die "photos"-Unterordner, die das Repository erwartet
        File(testCacheDirInTemp, "photos").mkdirs()
        File(testFilesDirInTemp, "photos").mkdirs()

        // Erstelle einen Spy vom Robolectric Context, um cacheDir und filesDir zu überschreiben,
        // damit sie auf unsere temporären Ordner zeigen.
        // Das ist ein Weg, dem Repository kontrollierte Pfade unterzujubeln, ohne das Repository selbst ändern zu müssen.
        val spiedContext = spyk(context) {
            every { cacheDir } returns testCacheDirInTemp
            every { filesDir } returns testFilesDirInTemp
        }

        repository = PhotoSaverRepository(spiedContext, mockContentResolver)
        println("SETUP: Repository instance in setUp: ${System.identityHashCode(repository)}") // Logge den Identity Hash
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Coroutine-Dispatcher zurücksetzen
        // tempFolder.delete() wird automatisch durch die @Rule aufgerufen,
        // sodass eine manuelle Löschung der Ordner meist nicht nötig ist.
    }

    // --- Tests für generatePhotoCacheFile ---
    @Test
    fun `generatePhotoCacheFile should return file in correct cache photos directory`() {
        val file = repository.generatePhotoCacheFile()
        assertTrue("Datei sollte im Cache-Verzeichnis 'photos' liegen", file.absolutePath.startsWith(File(testCacheDirInTemp, "photos").absolutePath))
        assertTrue("Datei sollte mit .jpg enden", file.name.endsWith(".jpg"))
    }

    // --- Tests für cacheFromUri ---
    @Test
    fun `cacheFromUri should copy data from uri to cache file and set photo variable`() = runTest(testDispatcher) {
        // Given
        val inputStream = ByteArrayInputStream(testPhotoBytes)
        every { mockContentResolver.openInputStream(testUri) } returns inputStream

        // When
        repository.cacheFromUri(testUri)

        // Ensure all coroutines launched by cacheFromUri (if any internally use a dispatcher) complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull("Internes 'photo' sollte nach dem Cachen nicht null sein", repository.photo)
        val cachedFile = repository.photo!!
        assertTrue("Cache-Datei sollte existieren", cachedFile.exists())
        assertEquals("Inhalt der Cache-Datei stimmt nicht überein", testPhotoData, cachedFile.readText())
        assertTrue("Cache-Datei sollte im Test-Cache-Photos-Ordner liegen", cachedFile.absolutePath.startsWith(File(testCacheDirInTemp, "photos").absolutePath))

        verify(exactly = 1) { mockContentResolver.openInputStream(testUri) }
    }

    @Test
    fun `cacheFromUri should do nothing and photo should be null if contentResolver returns null stream`() = runTest(testDispatcher) {
        // Given
        every { mockContentResolver.openInputStream(testUri) } returns null

        // When
        repository.cacheFromUri(testUri)

        // Then
        assertNull("Internes 'photo' sollte null sein, wenn Stream null ist", repository.photo)
        // Sicherstellen, dass keine unerwarteten Dateien erstellt wurden
        assertEquals("Cache-Photos-Ordner sollte leer sein", 0, File(testCacheDirInTemp, "photos").listFiles()?.size ?: 0)
    }

    @Test
    fun `cacheFromUri should do nothing and photo should be null if openInputStream throws FileNotFoundException`() = runTest(testDispatcher) {
        // Given
        every { mockContentResolver.openInputStream(testUri) } throws FileNotFoundException("Test Exception")

        // When
        repository.cacheFromUri(testUri)

        // Then
        assertNull("Internes 'photo' sollte null sein bei Exception", repository.photo)
        assertEquals("Cache-Photos-Ordner sollte leer sein", 0, File(testCacheDirInTemp, "photos").listFiles()?.size ?: 0)
    }

    // --- Tests für removeFile ---
    @Test
    fun `removeFile should delete cached photo and set photo to null`() = runTest(testDispatcher) {
        // Given: Erstelle eine Dummy-Datei im Cache, die das Repository "kennt"
        val cachedPhotoFile = repository.generatePhotoCacheFile() // Holen wir uns einen Pfad aus dem Repo
        cachedPhotoFile.writeText("some data")
        repository.photo = cachedPhotoFile // Simuliere, dass diese Datei gecached wurde
        assertTrue("Dummy-Cache-Datei sollte vor dem Test existieren", cachedPhotoFile.exists())

        // When
        val result = repository.removeFile()

        // Then
        assertTrue("removeFile sollte true zurückgeben", result)
        assertNull("Internes 'photo' sollte nach dem Löschen null sein", repository.photo)
        assertFalse("Cache-Datei sollte nach removeFile nicht mehr existieren", cachedPhotoFile.exists())
    }

    @Test
    fun `removeFile should return true and set photo to null if photo is already null`() = runTest(testDispatcher) {
        // Given
        repository.photo = null // Kein Foto gecached

        // When
        val result = repository.removeFile()

        // Then
        assertTrue("removeFile sollte true zurückgeben", result)
        assertNull("Internes 'photo' sollte null bleiben", repository.photo)
    }

    @Test
    fun `removeFile should return true and set photo to null if photo file does not exist`() = runTest(testDispatcher) {
        // Given
        val nonExistentFile = repository.generatePhotoCacheFile() // Holen wir uns einen Pfad
        // Sicherstellen, dass die Datei nicht existiert
        if (nonExistentFile.exists()) nonExistentFile.delete()
        repository.photo = nonExistentFile // Simuliere, dass dieses (nicht existente) Foto gecached war

        // When
        val result = repository.removeFile()

        // Then
        assertTrue("removeFile sollte true zurückgeben", result)
        assertNull("Internes 'photo' sollte null sein", repository.photo)
        assertFalse("Datei sollte immer noch nicht existieren", nonExistentFile.exists())
    }

    // --- Tests für savePhoto ---
    @Test
    fun `savePhoto should copy cached file to persistent storage, delete cached file, and return saved file`() = runTest(testDispatcher) {
        // Given: Ein gecachtes Foto
        val cachedPhotoFile = repository.generatePhotoCacheFile()
        cachedPhotoFile.writeText(testPhotoData)
        repository.photo = cachedPhotoFile
        assertTrue("Cache-Datei sollte vor dem Speichern existieren", cachedPhotoFile.exists())

        // When
        val savedFile = repository.savePhoto()

        // Then
        assertNotNull("Gespeicherte Datei sollte nicht null sein", savedFile)
        savedFile?.let { file ->
            assertTrue("Gespeicherte Datei sollte existieren", file.exists())
            assertEquals("Inhalt der gespeicherten Datei stimmt nicht überein", testPhotoData, file.readText())
            assertTrue("Gespeicherte Datei sollte im Test-Files-Photos-Ordner liegen", file.absolutePath.startsWith(File(testFilesDirInTemp, "photos").absolutePath))
        }

        assertNull("Internes 'photo' sollte nach dem Speichern null sein", repository.photo)
        assertFalse("Ursprüngliche Cache-Datei sollte nach dem Speichern nicht mehr existieren", cachedPhotoFile.exists())
    }

    @Test
    fun `savePhoto should return null if no photo is cached`() = runTest(testDispatcher) {
        // Given
        repository.photo = null

        // When
        val savedFile = repository.savePhoto()

        // Then
        assertNull("Gespeicherte Datei sollte null sein, wenn kein Foto gecached ist", savedFile)
    }

    @Test
    fun `savePhoto should return null and not delete cache if cached photo file does not exist on disk`() = runTest(testDispatcher) {
        // Given
        val nonExistentCachedFile = repository.generatePhotoCacheFile()
        if (nonExistentCachedFile.exists()) nonExistentCachedFile.delete() // Sicherstellen, dass es nicht existiert
        repository.photo = nonExistentCachedFile

        // When
        val savedFile = repository.savePhoto()

        // Then
        assertNull("Gespeicherte Datei sollte null sein", savedFile)
        // 'photo' sollte durch die interne Logik von savePhoto (Prüfung auf Existenz) genullt werden
        assertNull("Internes 'photo' sollte null sein", repository.photo)
        assertFalse("Die (nicht existente) Cache-Datei sollte immer noch nicht existieren", nonExistentCachedFile.exists())
    }
}
