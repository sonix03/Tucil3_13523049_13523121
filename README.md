
#  Rush Hour Puzzle Solver

Program ini merupakan implementasi solver untuk puzzle **Rush Hour**, yaitu permainan logika di mana pemain harus memindahkan mobil-mobil di grid hingga mobil utama (biasanya warna merah) dapat keluar dari papan melalui titik **exit**.

Program ini mendukung berbagai algoritma pencarian pathfinding, yaitu:

- Greedy Best First Search (GBFS)
- Uniform Cost Search (UCS)
- A* Search
- Iterative Deepening A* (IDA*)

Disertai juga dengan **GUI interaktif** untuk memvisualisasikan langkah penyelesaian, mengatur kecepatan animasi, serta memilih file input dengan mudah.

---

## Requirements

Program ini ditulis menggunakan **Java 8+** dan membutuhkan:

- Java Development Kit (JDK) versi **8 atau lebih tinggi**
- Sistem operasi Windows / Linux / macOS yang mendukung Java
- Tidak membutuhkan library eksternal (hanya `javax.swing` dan `java.awt` bawaan Java)

---

## Struktur Folder

```
/Tucil3_13523049_13523121
├── src/
│   ├── AnimationManager.java
│   ├── AStar.java
│   ├── Board.java
│   ├── BoardGUI.java
│   ├── GUIFrame.java
│   ├── Heuristic.java
│   ├── IDAStar.java
│   ├── Main.java
│   ├── MainGUI.java
│   ├── Move.java
│   ├── Piece.java
│   ├── PieceOrientation.java
│   ├── Solver.java
│   ├── UCS.java
├── bin/
├── doc/
├── test/
│   ├── input/
│   ├── output/
├── runGUI.bat
```

Pastikan semua file `.java` disimpan di dalam folder `src/`.

---

## Cara Menjalankan Program

### Windows
./runGUI.bat

atau

javac -d bin src\*.java
java -cp bin MainGUI

### Mac
./runGUI.sh

atau

javac -d bin src/*.java
java -cp bin MainGUI


### Langkah-langkah Penggunaan:

1. Klik tombol **"Pilih File"** untuk memilih file input dari folder `test/input/`.
2. Pilih **algoritma pencarian** dan (jika tersedia) **heuristik** yang diinginkan.
3. Klik **"Jalankan"** untuk memulai pencarian dan menampilkan animasi langkah solusi.
4. Gunakan **slider kecepatan** di bawah GUI untuk mengatur kecepatan animasi secara real-time.
5. Setelah solusi selesai ditampilkan, kamu akan ditanya apakah ingin menyimpan hasil ke file.

---

## 👤 Author

Program ini dibuat oleh:

- **Nama**: Muhammad Fithra Rizki
- **NIM**: 13523049
dan
- **Nama**: Ahmad Wicaksono
- **NIM**: 13523121