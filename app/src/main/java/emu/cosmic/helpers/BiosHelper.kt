package emu.cosmic.helpers

import androidx.lifecycle.ViewModel
import emu.cosmic.data.BiosInfo
import emu.cosmic.data.CosmicSettings
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException

class BiosHelper : ViewModel() {
    companion object {
        private val settings = CosmicSettings.globalSettings
        var biosDir = File(settings.appStorage, "System")

        private val biosList = mutableListOf<BiosInfo>()
        private val biosPack: Array<out File>? get() = biosDir.listFiles()

        fun toDefault() {
            biosDir = File(settings.appStorage, "System")
            cleanAllBios()
            biosList.clear()
            settings.biosPath = ""
        }

        fun getInUse(defaultPos: Int): Int {
            biosPack?.forEachIndexed { index, bios ->
                if (bios.path == settings.biosPath)
                    return index
            }
            return getBios(defaultPos)
        }

        private external fun addBios(descriptor: FileDescriptor, position: Int): BiosInfo
        private external fun setBios(position: Int): Int
        private external fun removeBios(posFd: IntArray): Boolean
        private external fun cleanAllBios()
        private external fun getBios(defaultPos: Int): Int
    }

    init {
        if (!biosDir.exists())
            biosDir.mkdirs()
        assert(biosDir.exists() && biosDir.isDirectory)
    }

    fun getAllInstalled() : List<BiosInfo> {
        var position = 0
        biosPack?.forEach { biosFile ->
            runCatching {
                val resident = biosList.first {
                    it.biosPath == biosFile.name
                }
                resident
            }.onFailure {
                if (it is NoSuchElementException || it is NullPointerException)
                    loadBios(biosFile.path, position++)
            }
        }
        return biosList
    }
    private fun loadBios(filePath: String, position: Int) {
        val bios = File(filePath)
        // Validating if we are working in the application's root directory
        val biosName = bios.absolutePath
        bios.path.apply {
            assert(contains(biosDir.path))
        }
        val injection = runCatching {
            val biosStream = FileInputStream(bios)
            val model = addBios(biosStream.fd, position)
            if (model.biosName.isEmpty()) {
                throw IOException("BIOS $biosName not found in your storage or not accessible")
            }
            model.biosPath = bios.path
            model.fileAlive = biosStream
            model
        }
        if (injection.isSuccess) {
            if (!biosList.contains(injection.getOrThrow()))
                biosList.add(injection.getOrThrow())
        }
    }

    fun unloadBios(position: Int) {
        val model = biosList[position]
        val removed = runCatching {
            val validModelInfo = intArrayOf(0, model.position)
            if (!removeBios(validModelInfo)) {
                throw Exception("Unable to remove BIOS with fd, pos: $validModelInfo")
            }
        }
        if (removed.isSuccess)
            biosList.remove(model)
    }
    fun activateBios(position: Int) : Int {
        val previous = setBios(position)
        if (previous != position) {
            for (bios in biosList) {
                if (bios.position == previous) {
                    biosList[previous].selected = false
                }
            }
        }
        biosList[position].selected = true
        settings.biosPath = biosList[position].biosPath
        return previous
    }
}