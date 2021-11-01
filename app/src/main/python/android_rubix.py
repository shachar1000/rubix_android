from rubik_solver import Cubie, utils, Move # thistlethwaite, kociemba
import itertools
processMoves = lambda moves: list(itertools.chain(*[[movee[0] for i in range(int(movee[1].replace("'","3")))] if len(movee) is 2 else movee[0] for movee in moves]))
def generateMoves():
    cube1, cube2, cubeStringsScramble, cubeStringsSolve = Cubie.Cube(""), Cubie.Cube(""), "", ""
    shuffle_moves = [move.__str__().strip() for move in cube2.shuffle()]
    scrambleMovesString = ''.join(processMoves(shuffle_moves))
    for move in scrambleMovesString:
        cube1.move(Move.Move(move))
        cubeStringsScramble+=cube1.to_naive_cube(True)+","
    solveMovesString = ''.join(processMoves([move.__str__() for move in utils.solve(cube2.to_naive_cube(True), method="Kociemba")]))
    for move in solveMovesString:
        cube1.move(Move.Move(move))
        cubeStringsSolve+=cube1.to_naive_cube(True)+","
    return (scrambleMovesString+","+cubeStringsScramble)+(solveMovesString+","+cubeStringsSolve)[:-1]

print(generateMoves())
