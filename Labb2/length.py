#!/usr/bin/env python3
import os
import subprocess

munch_f = open("dir/Munch_tokenized.txt", "w+")
subprocess.call(['java', '-cp', 'classes;pdfbox',\
 'ir.TokenTest', '-f', 'davisWiki/Munch_Money.f',\
  '-p', 'patterns.txt', '-rp', '-cf'],\
   stdout = munch_f)
nina_f = open("dir/nina_tokenized.txt", "w+")
subprocess.call(['java', '-cp', 'classes;pdfbox',\
 'ir.TokenTest', '-f', 'davisWiki/NinadelRosario.f',\
  '-p', 'patterns.txt', '-rp', '-cf'],\
   stdout = nina_f)

munch_f.close()
nina_f.close()

munch_f = open("dir/Munch_tokenized.txt", "rU")
nina_f = open("dir/nina_tokenized.txt", "rU")

#exact_data = []
files = [munch_f,nina_f]
for file in files:
    m_dict = dict()
    for i,entry in enumerate(iter(file)):
        entry = entry.strip().lower()
        if entry in m_dict:
            m_dict[entry] = m_dict[entry] + 1
        else:
            m_dict[entry] = 1
    file.close()

    total_squared = 0
    total_notS = 0
    for key, value in m_dict.items():
        total_squared = total_squared + value**2
        total_notS = total_notS + value

    total = total_squared ** 0.5
    print(total)
    print(total_squared)

    print(total_notS)
