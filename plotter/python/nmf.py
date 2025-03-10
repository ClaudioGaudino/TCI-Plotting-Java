from py4j.java_gateway import JavaGateway, CallbackServerParameters, GatewayParameters
import numpy as np
from sklearn.decomposition import NMF


class NMFService(object):

    def factorize(self, matrix, k):
        V = np.array(matrix)
        model = NMF(n_components=k, init='random', random_state=0)
        W = model.fit_transform(V)
        H = model.components_
        return [W.tolist(), H.tolist()]

    def num(self, n):
        return n

    class Java:
        implements = ["data.NMF"]


if __name__ == "__main__":
    nmf = NMFService()
    gateway = JavaGateway(
        gateway_parameters=GatewayParameters(auto_convert=True),
        callback_server_parameters=CallbackServerParameters(),
        python_server_entry_point=nmf
    )
