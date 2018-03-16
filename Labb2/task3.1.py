import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import rbfnetwork as rbfNet

def squaredTransformation():

	X_train = np.arange(0.01,2*np.pi,0.01)
	X_test = np.arange(0.05,2*np.pi + 0.05,0.01)
	Y_train = rbfNet.gen_square_wave(X_train)
	Y_test = rbfNet.gen_square_wave(X_test)

	for i in range(10, 31):
		rbf = rbfNet.RbfNetwork((i,),'batch')
		rbf.train(X_train,Y_train)
		predicted_y = rbf.predict(X_test)
		predicted_y = np.where(predicted_y <= 0, -1, 1)
		predicted_y_train = rbf.predict(X_train)
		predicted_y_train = np.where(predicted_y_train <= 0, -1, 1)
		res_test = np.square(np.subtract(predicted_y, Y_test)).sum()/len(Y_test)
		res_train = np.square(np.subtract(predicted_y_train, Y_train)).sum()/len(Y_train)
		print(i, ", ", res_train,", ", res_test)
		plt.plot(X_train,Y_train)
		plt.plot(X_train, predicted_y_train)
		plt.savefig("images\square" + str(i) + "test.png")
		plt.close()


def batch_square():

	X_train = np.arange(0.01,2*np.pi,0.01)
	X_test = np.arange(0.05,2*np.pi + 0.05,0.01)
	Y_train = rbfNet.gen_square_wave(X_train)
	Y_test = rbfNet.gen_square_wave(X_test)

	for i in range(180, 201):
		rbf = rbfNet.RbfNetwork((i,),'batch')
		rbf.train(X_train,Y_train)
		predicted_y = rbf.predict(X_test)
		predicted_y_train = rbf.predict(X_train)
		res_test = np.square(np.subtract(predicted_y, Y_test)).sum()/len(Y_test)
		res_train = np.square(np.subtract(predicted_y_train, Y_train)).sum()/len(Y_train)
		print(i, ", ", res_train,", ", res_test)
		plt.plot(X_train,Y_train)
		plt.plot(X_train, predicted_y_train)
		plt.savefig("images\Batch" + str(i) + "test.png")
		plt.close()

def batch_sine():

	X_train = np.arange(0.01,2*np.pi,0.01)
	X_test = np.arange(0.05,2*np.pi + 0.05,0.01)
	Y_train = rbfNet.gen_sine_wave(X_train)
	Y_test = rbfNet.gen_sine_wave(X_test)

	for i in range(1, 21):
		rbf = rbfNet.RbfNetwork((i,),'batch')
		rbf.train(X_train,Y_train)
		predicted_y = rbf.predict(X_test)
		predicted_y_train = rbf.predict(X_train)
		res_test = np.square(np.subtract(predicted_y, Y_test)).sum()/len(Y_test)
		res_train = np.square(np.subtract(predicted_y_train, Y_train)).sum()/len(Y_train)
		print(i, ", ", res_train,", ", res_test)
		plt.plot(X_train,Y_train)
		plt.plot(X_train, predicted_y_train)
		plt.savefig("images\BatchSine" + str(i) + "test.png")
		plt.close()

batch_sine()
