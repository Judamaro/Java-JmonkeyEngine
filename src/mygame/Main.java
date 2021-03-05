package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener, AnimEventListener{

    //FISICAS
    //Herramientas para la materializacion del mapa
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    Spatial sceneModel;
    //Herramientas para la materializacion del mapa
    //FISICAS
    
    //JUGADOR
    //Herramientas para control del jugador
    //private CharacterControl player; //Añade un control
    //private Vector3f walkDirection; //Genera la direccion en el espacio con vectores
    //private boolean left, right, up, down; //Se crean los movimientos
    //private Vector3f camDir; //Direccion de camara
    //private Vector3f camLeft; //Direccion de camara
    //Herramientas para control del jugador
    //JUGADOR
    
    //JUGADOR OTO
    private CharacterControl player;
    private Vector3f walkDirection;
    private boolean left, right, up, down, leftClick;
    private Vector3f camDir;
    private Vector3f camLeft;
    private AnimChannel channel;
    private AnimControl control;
    private ChaseCamera camarapersonaje;
    Node OTO;
    //JUGADOR OTO
    
    
    
    //DISPARO
    private Node shootables; //
    private Geometry mark; //Marca asignada al momento del disparo
    //DISPARO

    public static void main(String[] args) {
        mygame.Main app = new mygame.Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {       
                
        //DISPARO
        //Creacion del CrossHairs, Keys y Mark
        initCrossHairs(); // a "+" en la mitad de la pantalla
        initKeys();       // load custom key mappings
        initMark();       // deja una marca roja al momento del disparo
        //Creacion del CrossHairs, Keys y Mark
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);
        shootables.attachChild(makeCube("a Dragon", 2f, 4f, 1f)); //Creacion de un cubo
        shootables.attachChild(makeCube("a tin can", 1f, 2f, 0f)); //Creacion de un cubo 
        shootables.attachChild(makeCube("the Sheriff", 0f, 3f, 2f)); //Creacion de un cubo
        shootables.attachChild(makeCube("the Deputy", 1f, 6f, 4f)); //Creacion de un cubo
        //shootables.attachChild(makeFloor());
        //DISPARO

        
        //Control del jugador absoluto
        //JUGADOR OTO
        OTO = new Node("Oto"); //Se crea el nodo de oto
        OTO = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.j3o"); //castea el spatial del nodo
        OTO.setLocalTranslation(10f, 15f, 10f); 
        control = OTO.getControl(AnimControl.class); 
        control.addListener(this);
        channel = control.createChannel();
        channel.setAnim("stand");             
        camDir = new Vector3f(); //Direccion player
        camLeft = new Vector3f(); //Direccion player
        walkDirection = new Vector3f(); //Direccion player
        left = false; //Izquierda action
        right = false; //Derecha action
        up = false; //Arriba action
        down = false; //Abajo action
        setUpKeys(); //Diosito asignacion de key metodo
        cambiarMovilidadCamara(); //El papá de diosito, cambiar movilidad camara
        //Control del jugador absoluto
        //JUGADOR OTO

        //Fisicas y colision del jugador
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        //JUGADOR OTO
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);

        player.setGravity(30f);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        //JUGADOR OTO
        //Fisicas y colision del jugador       

        
        //CAMARA
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        setUpKeys();
        setUpLight();
        //CAMARA
        
        
        //MAPA
        //Carga del mapa y asignacion de los elementos de collision y rigid
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);

        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        //Carga del mapa y asignacion de los elementos de collision y rigid
        //MAPA
        
        
        //JUGADOR OTO
        OTO.addControl(player);
        //JUGADOR OTO
        
        
        //FISICAS
        rootNode.attachChild(sceneModel);
        rootNode.attachChild(OTO);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player); //Fisicas jugador
        //FISICAS
        
        //JUGADOR OTO
        camarapersonaje = new ChaseCamera(cam, OTO, inputManager);
        //JUGADOR OTO
        
    }
    private final ActionListener actionListener = new ActionListener() {
        //DISPARO
        private boolean keyPressed;
        //DISPARO

        //DISPARO
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals("Shoot") && !keyPressed) { //Disparo ejecutado
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection()); //Rayo en direccion
                shootables.collideWith(ray, results); //Elemento shoot

                System.out.println("----- Collisions? " + results.size() + "-----");
                for (int i = 0; i < results.size(); i++) {
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                }
                if (results.size() > 0) { //Distancia de punteria o colision
                    CollisionResult closest = results.getClosestCollision();
                    Geometry g = closest.getGeometry();
                    
                    Material matl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    matl.setColor("Color", ColorRGBA.randomColor());
                    
                    g.setMaterial(matl);
                    
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark); //Añade una marca a un nodo
                } else {
                    rootNode.detachChild(mark);
                }
            } //Creacion de todo lo relacionado con el disparo, al igual del que pasa luego del disparo
        }
        //DISPARO
    };

    private void initKeys() {
        //DISPARO
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
        inputManager.addListener(actionListener, "Shoot");
        //DISPARO
    }

    private void initMark() {
        //DISPARO
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Cyan);
        mark.setMaterial(mark_mat);
        //DISPARO
    }

    private void initCrossHairs() {
        //DISPARO
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
        //DISPARO
    }

    protected Geometry makeCube(String name, float x, float y, float z) {
        //DISPARO
        Box box = new Box(1, 1, 1);
        Geometry cube = new Geometry(name, box);
        cube.setLocalTranslation(x, y, z);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.randomColor());
        cube.setMaterial(mat1);
        return cube;
        //DISPARO
    }

//    protected Geometry makeFloor() {
//        //DISPARO
//        Box box = new Box(15, .2f, 15);
//        Geometry floor = new Geometry("the Floor", box);
//        floor.setLocalTranslation(0, -4, -5);
//        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat1.setColor("Color", ColorRGBA.Gray);
//        floor.setMaterial(mat1);
//        return floor;
//        //DISPARO
//    }
    @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.6f); //Direccion de camara con mouse
        camLeft.set(cam.getLeft()).multLocal(0.4f); //Direccion de camara con mouse
        walkDirection.set(0, 0, 0); //Posicion del jugador
        //Creacion y asignacion de controles al juego
        if (left) {
            walkDirection.addLocal(camLeft);
            player.setViewDirection(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
            player.setViewDirection(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
            player.setViewDirection(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
            player.setViewDirection(camDir.negate());
        }
        //Creacion y asignacion de controles al juego

        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private void setUpKeys() {
        //Llamado de funciones por teclado
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Walk"); //Walk para la animacion 
    }

    //CAMARA
    //Esta clase cambia la mobilidad de la camara, la unica forma de mover la camara es presionando el click izq del mouse
    private void cambiarMovilidadCamara() {
        inputManager.deleteMapping("FLYCAM_RotateDrag");
        flyCam.setDragToRotate(true);
        inputManager.addMapping("FLYCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addListener(flyCam, "FLYCAM_RotateDrag");
    }
    //CAMARA

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Left")) {
            if (!channel.getAnimationName().equals("Walk")) {
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Cycle);
            }
            else{
                channel.setAnim("stand", 0.50f);
            }
            left = isPressed;
        } else if (name.equals("Right")) {
            if (!channel.getAnimationName().equals("Walk")) { //Se llama la animacion
                channel.setAnim("Walk", 0.50f); //Animacion de caminar asignada
                channel.setLoopMode(LoopMode.Cycle);
                
            }
              else{
                channel.setAnim("stand", 0.50f);
            }
            right = isPressed;
        } else if (name.equals("Up")) {
            if (!channel.getAnimationName().equals("Walk")) {
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Cycle);
            }
              else{
                channel.setAnim("stand", 0.50f);
            }
            up = isPressed;
        } else if (name.equals("Down")) {
            if (!channel.getAnimationName().equals("Walk")) {
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Cycle);
            }
              else{
                channel.setAnim("stand", 0.50f);
            }
            down = isPressed;
        } else if (name.equals("Jump")) {
            if (isPressed) {
                player.jump();
            } //OTO no tiene animacion de salto, por ende no se pone una como tal
        } else if (name.equals("pick")) {
            leftClick = isPressed;
        } else {
            leftClick = false;
            flyCam.setEnabled(true);
        }
    }

    private void setUpLight() {
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }
    
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Walk")) {
            channel.setAnim("Walk", 0.50f);
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setSpeed(1f);
        } //Animacion de caminar para OTO
    }
    
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }
}
