import warnings
from typing import List, Dict, Tuple, Any
from py4j.java_gateway import JavaGateway, CallbackServerParameters, GatewayParameters
from py4j.java_collections import ListConverter
import numpy as np
from sklearn.decomposition import NMF
from sklearn.exceptions import ConvergenceWarning
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_samples
from scipy.spatial.distance import pdist


class NMFService(object):

    def factorize(self, matrix, k):
        V = np.array(matrix)
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


def runModuleClustering(
        w_matrices: List[List[List[float]]],
        max_k: int = None,
        n_repeats: int = 10
) -> Dict[str, Any]:
    """
    Performs clustering and analysis of NMF activation matrices.

    Parameters:
        w_matrices (List of 2D Lists): Each is a [signals x modules] matrix per subject.
        max_k (int): Maximum number of clusters to test. Defaults to number of subjects.
        n_repeats (int): Number of repetitions for silhouette evaluation per k.

    Returns:
        Dictionary with:
            - 'k_optimal': int
            - 'assignments': List of tuples (subject_index, module_index, cluster_index)
            - 'median_profiles': List of lists (clusters x signals)
            - 'std_profiles': List of lists (clusters x signals)
            - 'intra_similarity': List of (mean, std) cosine similarity per cluster
            - 'inter_similarity': 2D list (cluster x cluster cosine similarity matrix)
    """

    # Normalize each matrix with min-max scaling
    activations = []
    for M in w_matrices:
        M = np.array(M)
        min_val, max_val = np.min(M), np.max(M)
        if max_val > min_val:
            M_norm = (M - min_val) / (max_val - min_val)
        else:
            M_norm = np.zeros_like(M)
        activations.append(M_norm)

    # Build flat data matrix
    data = []
    subject_indices = []
    module_indices = []

    for subj_idx, M in enumerate(activations):
        for mod_idx in range(M.shape[1]):
            data.append(M[:, mod_idx].tolist())
            subject_indices.append(subj_idx)
            module_indices.append(mod_idx)

    data_np = np.array(data)
    num_signals = data_np.shape[1]
    num_subjects = len(w_matrices)
    max_k = max_k or num_subjects

    # Silhouette analysis
    silhouette_scores = []
    for k in range(2, max_k + 1):
        s_k = []
        for _ in range(n_repeats):
            kmeans = KMeans(n_clusters=k, max_iter=50, random_state=1).fit(data_np)
            s = silhouette_samples(data_np, kmeans.labels_)
            s_k.extend(s)
        silhouette_scores.append((k, float(np.mean(s_k)), float(np.std(s_k))))

    silhouette_scores.sort(key=lambda x: -x[1])
    k_optimal = silhouette_scores[0][0]

    # Final clustering
    final_kmeans = KMeans(n_clusters=k_optimal, max_iter=50, random_state=1).fit(data_np)
    labels = final_kmeans.labels_

    # Assignments
    assignments = list(zip(subject_indices, module_indices, labels.tolist()))

    # Cluster statistics
    median_profiles = []
    std_profiles = []
    intra_similarity = []

    for cluster in range(k_optimal):
        cluster_rows = data_np[labels == cluster]
        if cluster_rows.size == 0:
            median_profiles.append([0.0] * num_signals)
            std_profiles.append([0.0] * num_signals)
            intra_similarity.append((None, None))
            continue

        median = np.median(cluster_rows, axis=0).tolist()
        std_dev = np.std(cluster_rows, axis=0).tolist()
        median_profiles.append(median)
        std_profiles.append(std_dev)

        if cluster_rows.shape[0] > 1:
            cosine_distances = pdist(cluster_rows, metric='cosine')
            similarities = 1 - cosine_distances
            intra_similarity.append((float(np.mean(similarities)), float(np.std(similarities))))
        else:
            intra_similarity.append((None, None))

    # Inter-cluster similarity
    inter_similarity = [[None] * k_optimal for _ in range(k_optimal)]
    for i in range(k_optimal):
        v1 = np.array(median_profiles[i])
        norm1 = np.linalg.norm(v1)
        for j in range(k_optimal):
            v2 = np.array(median_profiles[j])
            norm2 = np.linalg.norm(v2)
            if norm1 > 0 and norm2 > 0:
                sim = float(np.dot(v1, v2) / (norm1 * norm2))
                inter_similarity[i][j] = sim

    return {
        'k_optimal': k_optimal,
        'assignments': assignments,
        'median_profiles': median_profiles,
        'std_profiles': std_profiles,
        'intra_similarity': intra_similarity,
        'inter_similarity': inter_similarity
    }


    class Java:
        implements = ["data.NMF", "data.Clusterer"]


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
