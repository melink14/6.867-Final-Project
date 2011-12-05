from pprint import pprint
from scipy import *
from scipy.sparse import *
from sklearn import cross_validation, datasets, svm
from sklearn.cross_validation import StratifiedKFold
from sklearn.grid_search import GridSearchCV
from sklearn.metrics import classification_report, precision_score, recall_score
from sklearn.svm import SVC, libsvm, LinearSVC
import numpy as np
import numpy as np
import re
import sklearn


#libsvm.set_verbosity_wrap(1)



DATA_SIZE = 2000
DATA_SET_SIZE = DATA_SIZE/4
FEATURE_SIZE = 4697

intmap = {}
intmapc = 0
def labelToInt(string):
    if string in intmap:
        return intmap[string]
    global intmapc
    intmapc += 1
    intmap[string] = intmapc
    
    return intmapc

def loadFile(filename):
    input = open(filename, "r")
    
    
    data = np.zeros((DATA_SET_SIZE, FEATURE_SIZE))
    target = np.zeros(DATA_SET_SIZE)
    
    
    pattern = "(.*)\\s(\\{.*\\})"
    reg = re.compile(pattern)
    
    rowI = 0;
    for line in input:
        mat = reg.match(line)
        if(not mat):
            continue
        
        parts = mat.group(1, 2)
        row = processSparseData(parts[1])
            
        
        target[rowI] = int(parts[0])
        
        for i in row:
            data[rowI][i] = 1;
            
        rowI += 1
        
        if rowI >= DATA_SET_SIZE:
            break
        
    return data, target
    
    
def processSparseData(line):
    indices = line.strip("{}").split(", ");
    
    if len(indices) == 1:
        if indices[0] == '':
            return []
    
    return map(int, indices)


if __name__ == "__main__":
    
    filenames= ("../../basicenemies.data","../../enemiesblocks.data","../../enemiesblocksgaps.data")
    
    data, targets = loadFile("../../basicgaps.data")
    
    for fn in filenames:
        print fn
        tdata, ttargets = loadFile(fn)
        data = np.vstack((data, tdata))
        targets = np.hstack((targets,ttargets))
        
    print "Starting"
    import time
    time.clock()
    # To apply an classifier on this data, we need to flatten the image, to
    # turn the data in a (samples, feature) matrix:
    n_samples = DATA_SIZE
    X = data
    y = targets
    
    # split the dataset in two equal part respecting label proportions
    train, test = iter(StratifiedKFold(y, 2)).next()
    
    ################################################################################
    # Set the parameters by cross-validation
    #tuned_parameters = [{'kernel': ['rbf'], 'gamma': [.5, .1, .01],
    #                     'C': [.001, .01, 10]}]
    
    tuned_parameters = [{'C': [.001, .01, 1, 10, 100, 1000]}]
    
    scores = [
        ('precision', precision_score),
        ('recall', recall_score),
    ]
   # 
    #for score_name, score_func in scores:
    clf = GridSearchCV(LinearSVC(C=1), tuned_parameters, n_jobs=4)
    clf.fit(X[train], y[train], cv=StratifiedKFold(y[train], 2))
    y_true, y_pred = y[test], clf.predict(X[test])

    print "Classification report for the best estimator: "
    print clf.best_estimator
#    print "Tuned for '%s' with optimal value: %0.3f" % (
#        score_name, score_func(y_true, y_pred))
    print classification_report(y_true, y_pred)
    print "Grid scores:"
    pprint(clf.grid_scores_)
    print
    
    print time.clock()
    
    from sklearn.externals import joblib
   # joblib.dump(clf.best_estimator, '../../svmgrid.pkl')
        
        
    #    clf = svm.SVC(C=512, kernel="rbf")
    #    #cv = cross_validation.ShuffleSplit(DATA_SIZE, n_iterations=3, test_fraction=.3, random_state=0)
    #    print cross_validation.cross_val_score(clf, data, targets, cv=2, score_func=sklearn.metrics.f1_score)
    #    #print clf.fit(data, targets)
        
    

