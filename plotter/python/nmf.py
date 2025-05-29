import warnings
from typing import List
from py4j.java_gateway import JavaGateway, CallbackServerParameters, GatewayParameters
from py4j.java_collections import ListConverter
import numpy as np
from sklearn.decomposition import NMF
from sklearn.exceptions import ConvergenceWarning
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_samples
from scipy.spatial.distance import pdist


def convert_java_2d_array(java_2d_array):
    try:
        outer_len = len(java_2d_array)
        result = []
        for i in range(outer_len):
            row = java_2d_array[i]
            # Defensive check: ensure row isn't broken
            try:
                inner_len = len(row)
                result.append([row[j] for j in range(inner_len)])
            except Exception as e:
                print(f"Failed to access row {i}: {e}")
                result.append([])
        return result
    except Exception as e:
        print(f"Failed to access outer array: {e}")
        return []



class KMeansResult():
    _instance = None

    def __init__(self):
        if not self.res:
            self.res = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super().__new__(cls)
            return cls._instance

    def set_res(self, res):
        self.res = res


class NMFService(object):

    def factorize(self, matrix, k):
        #matrix = convert_java_2d_array(matrix)
        matrix = [list(row) for row in matrix]  # ensure native Python list of lists
        V = np.array(matrix, dtype=float)
        model = NMF(n_components=k, init='random', random_state=0, max_iter=1000)
        with warnings.catch_warnings(record=True) as w:
            warnings.simplefilter("always", ConvergenceWarning)  # Always catch ConvergenceWarnings
            W = model.fit_transform(V)
            H = model.components_

        # Check for ConvergenceWarning
        for warning in w:
            if issubclass(warning.category, ConvergenceWarning):
                print("Warning: NMF did not converge within the maximum number of iterations.")

        return ListConverter().convert([W.tolist(), H.tolist()], gateway._gateway_client)

    class Java:
        implements = ["data.NMF"]


if __name__ == "__main__":
    print('[py]script started')
    nmf = NMFService()
    gateway = JavaGateway(
        gateway_parameters=GatewayParameters(auto_convert=True),
        callback_server_parameters=CallbackServerParameters(),
        python_server_entry_point=nmf
    )
    print('[py]gateway started')

    with open('tmp.flag', 'w') as f:
        f.write('ready')

    print('[py]file created')
