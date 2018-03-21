import numpy as np
import matplotlib.pyplot as plt
import os

data = []
with open('answer2.3.txt') as file:
    data = file.read()
data = data.split('\n')
data = data[:50]

num_docs = [10, 20, 30,40,50]
precision = []
recall = []
legends = []
for i,num in enumerate(num_docs):
    num_positives = 0
    for seq in data[:num]:
        if (seq[-1] != '0'):
            num_positives += 1
    recall.append(num_positives / 100)
    precision.append(num_positives / num)
    print(precision[i], recall[i])
    handle, = plt.plot(precision[i], recall[i],'*',label = str(num))
    legends.append(handle)
path = os.path.join("images/", 'precision_recal_graph' + '.png')
plt.xlabel('precision')
plt.ylabel('recall')
plt.plot(precision,recall)
plt.legend(legends, num_docs)
plt.savefig(path)
