(ns tools.excel.excel
  (:require
   [dk.ative.docjure.spreadsheet :as ss]))


;; Load a spreadsheet and read the first two columns from the
;; price list sheet:
(->> (ss/load-workbook "src/tools/excel/spreadsheet.xlsx")
     (ss/select-sheet "Test")
     (ss/select-columns {:A :name, :B :price}))


(ss/read-cell
 (->> (ss/load-workbook "src/tools/excel/spreadsheet.xlsx")
      (ss/select-sheet "Test")
      (ss/select-cell "A1")))

(->> (ss/load-workbook "src/tools/excel/spreadsheet.xlsx")
     (ss/select-sheet "Test")
     ss/row-seq
     (remove nil?)
     (map ss/cell-seq)
     (map #(map ss/read-cell %)))