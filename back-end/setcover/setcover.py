import sys
import numpy as np
from numpy import genfromtxt
from SetCoverPy import setcover

def main():
	matrix_file = sys.argv[1]
	cost_file = sys.argv[2]
	matrix = genfromtxt(matrix_file, delimiter=',', dtype=bool)
	cost = genfromtxt(cost_file, delimiter=',', dtype=int)
	try: 
		g = setcover.SetCover(matrix, cost)
		solution, used_time = g.SolveSCP()
		i = 0
		for b in g.s:
			if(b): 
				print("Selected Program: " + str(i))
			i+=1
	except ValueError as e:
		if str(e) == "There are uncovered rows! Please check your input!":
			print("Some input-output examples are not satisifed by any synthesized program. The set cover problem cannot be solved.")
		else:
			raise e

if __name__ == "__main__":
    main()