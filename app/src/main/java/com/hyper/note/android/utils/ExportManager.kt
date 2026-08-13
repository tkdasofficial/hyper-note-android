package com.hyper.note.android.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.hyper.note.android.data.Note
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportManager {
    fun exportAsPdf(context: Context, note: Note) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { textSize = 14f }
            
            var y = 50f
            paint.isFakeBoldText = true
            paint.textSize = 18f
            canvas.drawText(note.title.ifEmpty { "Untitled" }, 50f, y, paint)
            
            paint.isFakeBoldText = false
            paint.textSize = 14f
            y += 40f
            
            for (line in note.content.split("\n")) {
                canvas.drawText(line, 50f, y, paint)
                y += 20f
                if (y > 800f) break
            }
            
            document.finishPage(page)
            
            val filename = "Note_${System.currentTimeMillis()}.pdf"
            val outputStream = getOutputStream(context, filename, "application/pdf")
            if (outputStream != null) {
                document.writeTo(outputStream)
                outputStream.close()
                document.close()
                Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun exportAsZip(context: Context, notes: List<Note>) {
        try {
            val filename = "Notes_Export_${System.currentTimeMillis()}.zip"
            val outputStream = getOutputStream(context, filename, "application/zip")
            if (outputStream != null) {
                val zipOut = ZipOutputStream(outputStream)
                notes.forEachIndexed { index, note ->
                    val entryName = "${note.title.ifEmpty { "Note_$index" }}.txt"
                    zipOut.putNextEntry(ZipEntry(entryName))
                    val content = "${note.title}\n\n${note.content}".toByteArray(Charsets.UTF_8)
                    zipOut.write(content)
                    zipOut.closeEntry()
                }
                zipOut.close()
                Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to save ZIP", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting ZIP", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getOutputStream(context: Context, filename: String, mimeType: String): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { resolver.openOutputStream(it) }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            val file = java.io.File(downloadsDir, filename)
            java.io.FileOutputStream(file)
        }
    }
}
