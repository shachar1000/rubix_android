package com.example.a3d_rubiks;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import androidx.annotation.RequiresApi;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.List;
import com.example.a3d_rubiks.ListUtils.EnumeratedItem;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import java.util.function.Consumer;
import java.util.function.BiFunction;
import java.util.HashSet;
//@RequiresApi(api = Build.VERSION_CODES.N)
public class MyGLRenderer implements GLSurfaceView.Renderer {
    //public OpenGLES20Activity activity; public MyGLRenderer(OpenGLES20Activity activity) { this.activity = activity; }
    public static boolean isUpperCase(String s) { for (int i=0; i<s.length(); i++) { if (!Character.isUpperCase(s.charAt(i))) { return false; } } return true; }
    public static List<Character> convertStringToCharList(String str) {
        List<Character> chars = new ArrayList<>();
        for (char ch : str.toCharArray()) { chars.add(ch); } return chars;
    } //str.matches("^(.)\\1*$")
    public static boolean identicalChars(String substring) { if((new HashSet<Character>(convertStringToCharList(substring))).size()==1) {return true;} return false; };
    public static String transformToInverseNotation(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            try {
                if (identicalChars(str.substring(i, i + 3))) {
                    stringBuilder.append(Character.toString(str.charAt(i)).toLowerCase()); i = i + 2;
                } else { stringBuilder.append(Character.toString(str.charAt(i))); }
            } catch(StringIndexOutOfBoundsException exception) {
                stringBuilder.append(Character.toString(str.charAt(i)));
            }
        }
        return stringBuilder.toString();
    }
    @FunctionalInterface
    public interface multipleArgLambda<T,U,V,R> {public R apply(T t, U u, V v);}
    public static Float generalizedSmoothStep(int N, int x) {
        multipleArgLambda<Integer,Integer,Integer,Integer> clamp = (_x, lowerlimit, upperlimit) -> { if (_x < lowerlimit) _x = lowerlimit; if (_x > upperlimit) _x = upperlimit; return _x; };
        BiFunction<Integer,Integer,Float> pascalTriangle = (a,b) -> {Float result=1f; for(int i = 0; i < b; i++) {result*=(a-i)/(i+1);} return result;};
        Float result = 0f; int t = clamp.apply(x, 0, 1);
        for (int i = 0; i <= N; i++) {
            result += pascalTriangle.apply(-N-1,i) * pascalTriangle.apply(2*N+1,N-i) * (float)Math.pow(t, N+i+1);
        } return result;
    }
    static void combinationRecursive(int[] arr, int len, int startPosition, int[] result) {
        if (len==0) {Log.d(TAG,Arrays.toString(result)); return;}
        for (int i = startPosition; i <= arr.length-len; i++) {
            result[result.length-len] = arr[i];
            combinationRecursive(arr, len-1, i+1, result);
        }
    }
    private final HashMap<String, int[]> movesMap = new HashMap<String, int[]>(); String[] moves = {"D"};
    public final HashMap<String, float[]> colorsMap = new HashMap<String, float[]>();
    ArrayList<ArrayList<Integer>> colorMappingScheme = new ArrayList<ArrayList<Integer>>();
    //public final String cubeString = "owoyywyyrbrobbbggwbbwrrybgygbgoggrgywrwwooooyrobrwyrwg";
    //public final String cubeString = "ooogyrgwwwrrwbrbogwrbyryybrobbggbyoyyggoogrbwoygwwyrwb";
    public final String cubeString = "yyyyyyyyyooooooooobbbbbbbbbrrrrrrrrrgggggggggwwwwwwwww";

    public int[] index = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53};
    public int[] indexFixed = {0,1,2,3,4,5,6,7,8, 15,12,9,16,13,10,17,14,11, 24,25,26,21,22,23,18,19,20, 35,32,29,34,31,28,33,30,27, 44,43,42,41,40,39,38,37,36, 51,52,53,48,49,50,45,46,47};

    Integer[][] colorMappingSchemeArray = {{0,2,3,3},{0,0,2,1},{1,2,1,0},{1,0,4,5},{2,2,0,2},{2,0,5,4}};
    private static final String TAG = "MyGLRenderer";
    private final float[] mProjectionMatrix = new float[16];
    public volatile float angleX;
    public volatile float angleY;
    public volatile int moveCounter = 0;
    private float mAngle;
    public String[] scrambleMoves;
    public String[] solveMoves;
    final float cubeColor3[][] = {{1.0f, 0.0f, 0.0f, 1.0f,1.0f, 0.0f, 0.0f, 1.0f,1.0f, 0.0f, 0.0f, 1.0f,1.0f, 0.0f, 0.0f, 1.0f,1.0f, 0.0f, 0.0f, 1.0f,1.0f, 0.0f, 0.0f, 1.0f},                                       {0.0f, 1.0f, 0.0f, 1.0f,                            0.0f, 1.0f, 0.0f, 1.0f,                            0.0f, 1.0f, 0.0f, 1.0f,                            0.0f, 1.0f, 0.0f, 1.0f,                            0.0f, 1.0f, 0.0f, 1.0f,                            0.0f, 1.0f, 0.0f, 1.0f},                                        {0.0f, 0.0f, 1.0f, 1.0f,                            0.0f, 0.0f, 1.0f, 1.0f,                            0.0f, 0.0f, 1.0f, 1.0f,                            0.0f, 0.0f, 1.0f, 1.0f,                            0.0f, 0.0f, 1.0f, 1.0f,                            0.0f, 0.0f, 1.0f, 1.0f},                                        {1.0f, 1.0f, 0.0f, 1.0f,                            1.0f, 1.0f, 0.0f, 1.0f,                            1.0f, 1.0f, 0.0f, 1.0f,                            1.0f, 1.0f, 0.0f, 1.0f,                            1.0f, 1.0f, 0.0f, 1.0f,                            1.0f, 1.0f, 0.0f, 1.0f},                                       {1.0f, 1.0f, 1.0f, 1.0f,                            1.0f, 1.0f, 1.0f, 1.0f,                            1.0f, 1.0f, 1.0f, 1.0f,                            1.0f, 1.0f, 1.0f, 1.0f,                            1.0f, 1.0f, 1.0f, 1.0f,                            1.0f, 1.0f, 1.0f, 1.0f},                                        {1.0f, 0.55f, 0f, 1.0f,                            1.0f, 0.55f, 0f, 1.0f,                            1.0f, 0.55f, 0f, 1.0f,                            1.0f, 0.55f, 0f, 1.0f,                            1.0f, 0.55f, 0f, 1.0f,                            1.0f, 0.55f, 0f, 1.0f,}            };
    public int counterRotation = 0;
    public boolean lockFrame = false;
    public List<String> instructionsList;
    String instructions = "FFFRRRUBBRRRFFFLLLBBBFFLLLBLLLRUUURFFFBBRRRLL,yyyyyybbbbbwbbwbbwrrrrrrrrryggyggyggooooooooogggwwwwww,yyyyyywwwbbgbbgbbgrrrrrrrrrbggbggbggoooooooooyyywwwwww,yyyyyygggbbybbybbyrrrrrrrrrwggwggwggooooooooobbbwwwwww,yyryyrggrbbybbybbyrrbrrwrrwwwwgggggggooyooyoobbowwowwo,yybyywggwbbybbybbyrrorrorroggwggwggwroorooroobbywwywwg,yyoyyoggobbybbybbyrryrryrrgggggggwwwwoowooboobbrwwrwwr,gyygyyooorrybbybbygggrryrrgwoogggwwwbbywooboobbrwwrwwr,ogwgyyoooyryybygbygggrryrrgworggwwwwbwboobooybbrwwrrbb,rwwgyyooowrygbyobygggrryrrgwobggbwwrooboowybbbbrwwryyg,rwggyyoogwrygbyobyggrrrrrrgwgwwgorbboobyowwbbbbywwoyyo,rwrgyroogwrygbyobyggyrrorrorwwbggbowgobyowgbbbbwwwyyyo,rwygyoooowrygbyobyggwrryrrobbrogwwgwgobrowrbbbbgwwyyyg,rwygyoyyywrbgbbobgrrgrrgoywobrogwogwgobrowrbbwobwwyyyg,rwygyogbbwrwgboobborryrrwggybrygwygwgobrowrbbooowwyyyg,rwygyobowwrogboobowyogrrgrrgbrbgwbgwgobrowrbbyyywwyyyg,bwywyobowogwbbroooryogrrbrrgbrbgwbgwgoyrowrbywyygwygyg,ywywyoyowoboobgorwbyowrrbrrgbrbgwbgwgogrogrbwryygwybyg,wwygyogowooorbbwgoyyowrryrrgbrbgwbgwgobrogrbrbyywwybyg,rwwgyogowyoowbbwgoyyowrryrrgbgbgybgbrrgboorgbbyywwyorw,gybgyogowwoowbbrgoyyowrryrrgbwbgrbgorbrgorbogbyywwyyww,wrogyogowbooybbggoyyowrryrrgbwbgwbgybgroobgrrbyywwywwr,wrogyoobobobybyggyywyrryrrogbwogwwgybgroobgrrbbgwwywwr,wrogyoyybbobybbgggrryrrwoyyobwbgwogybgroobgrrwogwwywwr,rrobyorybgybgbogbbwrygrwyyyobwbgwogybgwoowgrwrogrwyowr,wrowyowybgggbbybobrrybrwryyobwbgwogybgooorgrrwoggwyywr,rroryooybbbgobgbygwrywrwwyyobwbgwogybgyooggrwrogbwyrwr,wwyryooybobgrbgrygwrywrwwyyobrbgwogrgobrogwgyrogbwybob,ywygyobybrroybbgggwryrrwoyyobrbgwogrgobrobwgrwogwwywob,rwybyobybgyrgbrgboyrygrwbyyobrbgwogrgowrowwgwwogrwyoob,wwywyowybgggbbyorrrrybrwbyyobrbgwogrgoororwgwyoggwybob,wwywywwyygggbbyorrrrgbrybyboboggbrwrboooorygwyowgwrbog,wwwyywywyrrgbbyorrobobrybybbooggbrwrgggoorygwyowgwrbog,yywwywywwobobbyorrboobrybybgggggbrwrrrgoorygwyowgwrbog,ywywyywwwboobbyorrgggbrybybrrgggbrwrobooorygwyowgwrbog,ywgwyywwbboobbyorrggwbrrbygrgrwgrrbgwboyorygwyoygwoboo,ywgwyyryoboybboorybbgyrggrwwgrwgrbbgwboyorygwrwrgwoboo,ywgwyyyoyborbbworrgybrrbwggrgrygrobgwboyorygwbwwgwoboo,ywgwyyrwrbobbbworwwrggrygbbygrogrybgwboyorygwoyrgwoboo,rrgwyyrwrgobwbwyrwwrggrygbbygoogoybbyywgobwrooyrgwobbo,oobwyyrwrgobrbwrrwwrggrygbbygoogbybbwgyroyobwoyrgwogwy,oogwyyrwbgobrbwrrwwrrgrogbyyoybggbborgyyoybbwoyogwrgww,oorwyorwygobrbwrrwwrogrrgbwbbybgoogybgyyoygbwoybgwygwr,ooowyrrwwgobrbwrrwwrbgrygbrobbggbyoyygyooyrbwoyggwygwb,wooyyrywwrrgrbowwborbwryrbrobbggbyoyyggoogrbowyggwygwb,ooogyrgwwwrrwbrbogwrbyryybrobbggbyoyyggoogrbwoygwwyrwb,FFFBUURRFDBRRDLLLBBUBBLLBBUUUBBUUUBBD,ooogyrgrrwrowbybogyywbrrrybgbbwgbwoyyggoogrbwygowwyrwb,ooogyrgyowrywbgboorbyyrybrwgbbrgbroyyggoogrbwwwgwwyrwb,ooogyrogywrwwbwbogbyrrrbwyygbbygbooyyggoogrbwrrgwwyrwb,bbygyrogyorwobwoogbyrrrbwyygbbygwoorroybogwggrrgwwywwb,ogbgybyrybyrobwooggbbrrbwyyroyygwoororwbogwggrrgwwywwb,ygorygybbgbbobwoogroyrrbwyyorwygwoorbyrbogwggrrgwwywwb,ygyrybybygbbobwoogrogrrywyboyoogrrwwbyrgogoggrrwwwbwwb,yggryyybbgbbobwoogrowrrbwybroowgywroyyrbogyggrrowwgwwb,yggryygwbgbrobrooowrryrobbwyoobgybroyyrbogyggwwrwwgwwb,yggryygwbgbrobryggwrryrooooyoobgybbwyyrbogbrowwwwwwbgr,oywryygwbgbrgbryggwrryrooooyorbggbbbbbyroyogrwwwwwwgoy,oyrryogwogbrgbryggwrwyrwooybbybgobgrbbyyoywgrwwowwrgob,oywrywgwygbrgbryggwroyrroobbbbggbroyobyooyrgrwwwwwygob,oywrywgwygbrgbrrgrwroyrryggbbbggboobobyooyroygwwowwbyw,yywyywywyrgggbbrrrororrrgggbbbggboobobbooorogwwwywwyyw,gywoywbwyrgrrbgrbgyroyrryggbbbggboobobyooyrowowwrwwgyw,wywyywywyrrrbbgggrgroorrbggbbbggboobobgoorrooywwywwyyw,bbbyywywywrrybgwgrgroorrbggbbwggyooyrooooborgywwywwrbg,wyyyywywybrrbbgbgrgroorrbggbbgggbooroorroogboywwywwwyw,yywwyyywygrobbgbgrbbgorrbggoorggboorbrrroogboywwywwwyw,rbrwyyywywroybgygrbbgorrbggoowggyoowgrbboroorywwywwgbb,wywwyyywyrrobbgrgrbbgorrbggoobggboogobgoorrrbywwywwwyy,bywryygwyrbrgbrrgowbgwrryggoobggboogobwooyrrybwwowwbyy,yywyyywwyrgrgbborrbbgrrrgggoobggboogobbooorrbwwwwwwyyy,bbgyyywwywgrybbyrrbbgrrrgggooyggyooyroorobbobwwwwwwrgo,yyyyyywwyggrbbbbrrbbgrrrgggooogggoorbrrooobbowwwwwwwyy,wyywyyyyybbgbbbbrrooorrrgggbrrgggoorggrooobbowwwwwwwyy,ywwyyyyyyooobbbbrrbrrrrrgggggrgggoorbbgooobbowwwwwwwyy,yyyyywyywbrrbbbbrrggrrrrgggbbggggooroooooobbowwwwwwwyy,ggryywyywyrrybbyrrggrrrrgggbbyggyoowboobooooowwwwwwbbb,yywyywyywrrrgbbgrrggrrrrgggbbbggboobobboooooowwwwwwyyy,yyyyyywwwggrgbbgrrbbbrrrgggobbggboobrrroooooowwwwwwyyy,wyywyywyybbbgbbgrrobbrrrgggrrrggboobggroooooowwwwwwyyy,wwwyyyyyyobbgbbgrrrrrrrrgggggrggboobbbboooooowwwwwwyyy,rbbyyyyyywbbwbbwrrrrrrrrgggggyggyooyoobooboobwwwwwwogg,yyyyyyyyybbbbbbrrrrrrrrrgggggggggooooooooobbbwwwwwwwww,yyyyyyyyybbbbbbbbbrrrrrrrrrgggggggggooooooooowwwwwwwww";
    private Cube[] cubeArray = new Cube[27]; int counter = 0;
    private int[] rangeIndex = IntStream.iterate(0, n->n+1).limit(3).toArray();
    public Boolean initialized = false;
    public int indexSolveMoves = 0;
    public static int delayStringCubeMapping = 0; // because now RRR is r but we still have cubestrings for RRR
    public static Boolean lowerCase = false; // lowercase means inverse rotation
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        for (int z:rangeIndex) {for (int y:rangeIndex) {for (int x:rangeIndex) { int[] args = {x,y,z}; cubeArray[counter] = new Cube(args); counter++; }}}
        movesMap.put("F", new int[] {2, 2, -1}); // axis 0, slice 2 is R, direction is -1 (inversion needed)
        movesMap.put("L", new int[] {0, 0, 1});
        movesMap.put("U", new int[] {1, 2, -1});
        movesMap.put("R", new int[] {0, 2, -1});
        movesMap.put("B", new int[] {2, 0, 1});
        movesMap.put("D", new int[] {1, 0, 1});
        List<String> colors = Arrays.asList("R", "G", "B", "Y", "W", "O");
        for (EnumeratedItem<String> color : ListUtils.enumerate(colors)) { colorsMap.put(color.item, cubeColor3[color.index]); }
        for (Integer[] face : colorMappingSchemeArray) {
            ArrayList<Integer> converted = Arrays.stream(face).collect(Collectors.toCollection(ArrayList::new));
            colorMappingScheme.add(converted);
        }
        instructionsList = Stream.of(instructions.split(",")).map(String::trim).collect(toList());
        for (int i = 0; i < instructionsList.size(); i++) { if (isUpperCase(instructionsList.get(i))) { indexSolveMoves = i; } }
        scrambleMoves = instructionsList.get(0).split(""); solveMoves = instructionsList.get(indexSolveMoves).split(""); // bruh

        scrambleMoves = transformToInverseNotation(String.join("", scrambleMoves)).split("");
        solveMoves = transformToInverseNotation(String.join("", solveMoves)).split("");

        Log.d(TAG, "test");
        Log.d(TAG, Arrays.toString(instructionsList.toArray()));
        Log.d(TAG, String.join("", scrambleMoves));
        // Iterator<String> iterator = map.KeySet().iterator();
    }
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (moveCounter == 0) { if (initialized) { } else { mapColors(cubeString); initialized = true; }} else {
            if (OpenGLES20Activity.getStartScramble()) { mapColors(instructionsList.get(moveCounter+delayStringCubeMapping)); }
            else if (OpenGLES20Activity.getStartSolve()) { mapColors(instructionsList.get(moveCounter+indexSolveMoves+delayStringCubeMapping)); }
        }
        if (OpenGLES20Activity.getStartScramble()) { if (moveCounter >= scrambleMoves.length) { OpenGLES20Activity.setStartScramble(false); moveCounter = 0; return; } }
        if (OpenGLES20Activity.getStartSolve()) { if (moveCounter >= solveMoves.length) { OpenGLES20Activity.setStartSolve(false); return; } }
        //cubeArray[i].getAngleAnimation() % 90f != 0f
        for (int i = 0; i < cubeArray.length; i++) {
            int[] value = {0,0,0,0,0,0,0}; // retarded nonsense
            if (OpenGLES20Activity.getStartScramble()) { value = movesMap.get(scrambleMoves[moveCounter].toUpperCase()); lowerCase = Character.isLowerCase((scrambleMoves[moveCounter]).charAt(0)); }
            else if (OpenGLES20Activity.getStartSolve()) { value = movesMap.get(solveMoves[moveCounter].toUpperCase()); lowerCase = Character.isLowerCase((solveMoves[moveCounter]).charAt(0));}
            float signOfRotation = 1f; if (lowerCase) {signOfRotation = -1;}
            if (OpenGLES20Activity.getStartScramble() || OpenGLES20Activity.getStartSolve()) {
                //Log.d("my array", Arrays.toString(cubeArray[i].getOffsets()));
                if (cubeArray[i].getOffsets()[value[0]] == value[1]) {
                    //Log.d("my array", Arrays.toString(cubeArray[i].getOffsets()));
                    if (counterRotation < 18) // 31
                        cubeArray[i].setAngleAnimation(cubeArray[i].getAngleAnimation() + value[2] * 5f * signOfRotation); // 3f
                } // else { moveCounter++; }
            }
            cubeArray[i].draw(mProjectionMatrix, value[0], angleX, angleY);
        }
        if (OpenGLES20Activity.getStartScramble() || OpenGLES20Activity.getStartSolve()) {
            if (counterRotation < 18) {
                counterRotation++;
            } else {
                if (lowerCase) {delayStringCubeMapping += 2;}
                counterRotation = 0;
                moveCounter++;
                for (int i = 0; i < cubeArray.length; i++) {
                    cubeArray[i].setAngleAnimation(0);
                }
            }
        }
    }

    public int[][] matrixReshape(int[][] nums, int r, int c) {
        int totalElements = nums.length * nums[0].length;
        if (totalElements != r * c || totalElements % r != 0) {
            return nums;
        }
        final int[][] result = new int[r][c];
        int newR = 0;
        int newC = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < nums[i].length; j++) {
                result[newR][newC] = nums[i][j];
                newC++;
                if (newC == c) {
                    newC = 0;
                    newR++;
                }
            }
        }
        return result;
    }
    public void mapColors(String cubeString) {
        char[] charifiedCubeString = cubeString.toCharArray(); String copiedCubeString = cubeString;
        for (int i = 0; i < index.length; i++) { charifiedCubeString[index[i]] = copiedCubeString.charAt(indexFixed[i]); }
        cubeString = String.valueOf(charifiedCubeString);
        int[] counters = IntStream.generate(() -> 0).limit(6).toArray();
        for (int i = 0; i < cubeArray.length; i++) {
            for (EnumeratedItem<ArrayList<Integer>> face : ListUtils.enumerate(colorMappingScheme)) {
                if (cubeArray[i].getOffsets()[face.item.get(0)] == face.item.get(1)) {
                    int indexInCubeString = 9*face.item.get(3)+counters[face.index];
                    float[] color = colorsMap.get(cubeString.substring(indexInCubeString, indexInCubeString+1).toUpperCase());
                    cubeArray[i].setColor(face.item.get(2), color);
                    counters[face.index] = counters[face.index] + 1;
                }
            }
        }
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
    }
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    public float getAngleX() {
        return angleX;
    }
    public void setAngleX(float angle) {
        angleX = angle;
    }
    public float getAngleY() {
        return angleY;
    }
    public void setAngleY(float angle) {
        angleY = angle;
    }

}
