package emu.zenith.helpers

import android.content.Context
import emu.zenith.data.KernelModel
import emu.zenith.data.ZenithSettings
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException

class KernelsHelper(val context: Context) {
    companion object {
        private val kernelsList = mutableListOf<KernelModel>()
    }
    private val globalSettings = ZenithSettings.globalSettings

    private val kernelsDir: File = File(globalSettings.rootDirectory + "/Kernels")
    init {
        if (!kernelsDir.exists())
            kernelsDir.mkdirs()
        assert(kernelsDir.exists() && kernelsDir.isDirectory)
    }

    fun getKernels() : List<KernelModel> {
        var position = 0
        kernelsDir.listFiles()?.forEach { biosFile ->
            runCatching {
                val resident = kernelsList.first {
                    it.biosFilename == biosFile.name
                }
                resident
            }.onFailure {
                if (it is NoSuchElementException || it is NullPointerException)
                    loadKernelModel(biosFile.path, position++)
            }
        }
        return kernelsList
    }
    private fun loadKernelModel(filePath: String, position: Int) {
        val kernelFile = File(filePath)
        // Validating if we are working in the application's root directory
        val kernelName = kernelFile.absolutePath
        kernelFile.path.apply {
            assert(contains(kernelsDir.path))
        }
        val injection = runCatching {
            val kernelStream = FileInputStream(kernelFile)
            val model = kernelAdd(kernelStream.fd, position)
            if (model.biosName.isEmpty()) {
                throw IOException("Kernel $kernelName not found in your storage or not accessible")
            }
            model.biosFilename = kernelFile.name
            model.fileAlive = kernelStream
            model
        }
        if (injection.isSuccess)
            kernelsList.add(injection.getOrNull()!!)
    }

    fun deleteKernel(position: Int) {
        val model = kernelsList[position]
        val removed = runCatching {
            val validModelInfo = intArrayOf(0, model.position)
            if (!kernelRemove(validModelInfo)) {
                throw Exception("Unable to remove kernel with fd, pos: $validModelInfo")
            }
        }
        if (removed.isSuccess)
            kernelsList.remove(model)
    }

    fun activateKernel(position: Int) : Int {
        val model = kernelsList[position]
        val previous = kernelSet(position)

        if (previous != model.position) {
            for (kernel in kernelsList) {
                if (kernel.position == previous) {
                    assert(kernel.selected)
                    kernel.selected = false
                }
            }
        }
        model.selected = true
        return previous
    }

    private external fun kernelAdd(descriptor: FileDescriptor, position: Int): KernelModel
    private external fun kernelSet(position: Int): Int
    private external fun kernelRemove(posFd: IntArray): Boolean
    external fun kernelRunning(defaultPos: Int): Int
}