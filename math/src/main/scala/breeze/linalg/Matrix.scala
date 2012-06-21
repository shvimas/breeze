package breeze.linalg

import scala.{specialized=>spec}
import breeze.storage.Storage
import org.netlib.blas.Dgemm
import breeze.util.Terminal

/**
 *
 * @author dlwh
 */

trait MatrixLike[@spec E, +Self <: Matrix[E]] extends Tensor[(Int, Int), E] with TensorLike[(Int, Int), E, Self] {

}

trait Matrix[@spec E] extends MatrixLike[E, Matrix[E]] {



  final def apply(i: (Int, Int)) = apply(i._1, i._2)
  final def update(i: (Int, Int), e: E) {
    update(i._1, i._2, e)
  }

  def apply(i: Int, j: Int):E
  def update(i: Int, j: Int, e: E)

  def size = rows * cols
  def rows: Int
  def cols: Int

  def keySet: Set[(Int, Int)] = new Set[(Int, Int)] {
    def contains(elem: (Int, Int)): Boolean = elem._1 >= 0 && elem._1 < rows && elem._2 >= 0 && elem._2 < cols

    def +(elem: (Int, Int)): Set[(Int, Int)] = Set() ++ iterator + elem
    def -(elem: (Int, Int)): Set[(Int, Int)] = Set() ++ iterator - elem

    def iterator: Iterator[(Int, Int)] = for{ j <- Iterator.range(0, cols); i <- Iterator.range(0, rows)} yield (i, j)
  }

  def iterator = for(i <- Iterator.range(0, rows); j <- Iterator.range(0, cols)) yield (i -> j) -> apply(i, j)

  def valuesIterator = for(i <- Iterator.range(0, rows); j <- Iterator.range(0, cols)) yield apply(i, j)

  def keysIterator = for(i <- Iterator.range(0, rows); j <- Iterator.range(0, cols)) yield (i -> j)

  def toString(maxLines : Int = Terminal.terminalHeight - 3,
               maxWidth : Int = Terminal.terminalWidth) : String = {
    val showRows = if (rows > maxLines) maxLines - 1 else rows
    def colWidth(col : Int) =
      (0 until showRows).map(row => this(row,col).toString.length+2).max

    val colWidths = new scala.collection.mutable.ArrayBuffer[Int]
    var col = 0
    while (col < cols && colWidths.sum < maxWidth) {
      colWidths += colWidth(col)
      col += 1
    }

    // make space for "... (K total)"
    if (colWidths.size < cols) {
      while (colWidths.sum + cols.toString.length + 12 >= maxWidth) {
        if (colWidths.isEmpty) {
          return "%d x %d matrix".format(rows, cols)
        }
        colWidths.remove(colWidths.length - 1)
      }
    }

    val newline = System.getProperty("line.separator")

    val rv = new scala.StringBuilder
    for (row <- 0 until showRows; col <- 0 until colWidths.length) {
      val cell = this(row,col).toString
      rv.append(cell)
      rv.append(" " * (colWidths(col) - cell.length))
      if (col == colWidths.length - 1) {
        if (col < cols - 1) {
          rv.append("...")
          if (row == 0) {
            rv.append(" (")
            rv.append(cols)
            rv.append(" total)")
          }
        }
        if (row + 1 < showRows) {
          rv.append(newline)
        }
      }
    }

    if (rows > showRows) {
      rv.append(newline)
      rv.append("... (")
      rv.append(rows)
      rv.append(" total)")
    }

    rv.toString
  }

  override def toString : String = toString(Terminal.terminalHeight, Terminal.terminalWidth)

}


trait StorageMatrix[@spec E] extends Matrix[E] with MatrixLike[E, StorageMatrix[E]] with Storage[E] {
  def offset: Int
  def majorStride: Int
  def isTranspose: Boolean



}