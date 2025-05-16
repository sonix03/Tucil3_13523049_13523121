# Tucil3_13523049_13523121

## Cara Run Program
### Windows
./runGUI.bat
### Mac
./runGUI.sh

## masalah Sekarang

1. blom handle kalau piece nya itu hanya boleh bergerak vertical atau horizontal
contoh: kasus sekarang 
DD atau DDD = bisa gerak vertikal harusnya gaboleh

D        D    
D  atau  D    -> bisa gerak horizotal harusnya gaboleh
         D
(udah fixxx)


2. blom handle test yang salah (udah di fix)


3. Mungkin ada heuristik dalam menyatakan langkah yang berulang misal:

P-kanan P-kanan P-kanan itu dihitung 3 langkah
lebih bagusnya kita membuat p-kanan3 yang dihitung 1 langkah
