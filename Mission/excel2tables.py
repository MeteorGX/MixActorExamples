# Python Excel Export Tables
# pip install xlrd
import os
import xlrd
import collections
import json
import codecs


# Search Excels
def excels(path):
    all_files = os.listdir(path)
    filtered = [file for file in all_files if file.endswith(".xls")]
    return filtered


# Excel Convert JSON
def excel2json(excelPath, tablePath, keyRow=1, typeRow=2, valueRow=3):
    wb = xlrd.open_workbook(excelPath)
    sheet = wb.sheet_by_index(0)
    keys = sheet.row_values(keyRow)
    types = sheet.row_values(typeRow)
    data = {}

    for row in range(valueRow, sheet.nrows):
        rowData = sheet.row_values(row)
        temp = collections.OrderedDict()
        for col in range(sheet.ncols):
            key = keys[col]
            colType = types[col]
            value = rowData[col]
            if colType == 'any':
                temp[key] = json.loads(value)
            elif colType == 'int':
                temp[key] = int(value)
            else:  # string float在读取excel时已区分
                temp[key] = value
        data[int(rowData[0])] = temp

    jsonStr = json.dumps(data, ensure_ascii=False, indent=4)
    with codecs.open(tablePath, "w", "utf-8") as f:
        f.write(jsonStr)

    return jsonStr


# params
excel_dir = "/excels"
table_dir = "/tables"

# Get Root Path
root_path = os.path.abspath('.')
excel_files = excels(root_path + excel_dir)

# Each Export Json
for f in excel_files:
    excel_name = f
    table_name = f.replace(".xls", ".json")
    excel_f = os.path.abspath(root_path + excel_dir + "/" + f)
    table_f = os.path.abspath(root_path + table_dir + "/" + table_name)
    print(excel_f + " -> " + table_f)
    excel2json(excel_f, table_f)
