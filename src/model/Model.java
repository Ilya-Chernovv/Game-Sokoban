package model;

import controller.EventListener;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Model {
    public static final int FIELD_CELL_SIZE = 20;   //размер ячейки игрового поля

    private GameObjects gameObjects;
    private int currentLevel = 1;
    private EventListener eventListener;
    LevelLoader levelLoader;

    public Model() {
        try {
            levelLoader = new LevelLoader(Paths.get(getClass().getResource("../res/levels.txt").toURI()));
        } catch (URISyntaxException e) {
        }
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void move(Direction direction) {
        /**
         * Проверить столкновение со стеной, если есть столкновение - выйти из метода.
         * Проверить столкновение с ящиками, если есть столкновение - выйти из метода.
         * Передвинуть игрока в направлении direction.
         * Проверить завершен ли уровень.
         */

        if (checkWallCollision(gameObjects.getPlayer(), direction)) {
            return;
        }

        if (checkBoxCollisionAndMoveIfAvailable(direction)) {
            return;
        }

        int dx = direction == Direction.LEFT ? -FIELD_CELL_SIZE : (direction == Direction.RIGHT ? FIELD_CELL_SIZE : 0);
        int dy = direction == Direction.UP ? -FIELD_CELL_SIZE : (direction == Direction.DOWN ? FIELD_CELL_SIZE : 0);
        gameObjects.getPlayer().move(dx, dy);

        checkCompletion();
    }

    public void restart() {
        //перезапускает текущий уровень, вызывая restartLevel с нужным параметром.
        restartLevel(currentLevel);
    }

    public void startNextLevel() {
        currentLevel++;
        restartLevel(currentLevel);
    }

    public boolean checkWallCollision(CollisionObject gameObject, Direction direction) {
        /**
         * метод проверяет столкновение со стеной. 
         * Он должен вернуть true, если при движении объекта gameObject в направлении direction произойдет столкновение 
         * в направлении direction произойдет столкновение со стеной, иначе false.
         */

        for (Wall wall : gameObjects.getWalls()) {
            if (gameObject.isCollision(wall, direction)) {
                return true;
            }
        }
        return false;
    }


    public boolean checkBoxCollisionAndMoveIfAvailable(Direction direction) {
        //Этот метод проверяет столкновение с ящиками.

        /**Возвращает true, если игрок не может быть сдвинут в направлении direction
         * Там находится: или ящик, за которым стена; или ящик за которым еще один ящик.
         */

        /**Возвращает false, если игрок может быть сдвинут в направлении direction
         * там находится: или свободная ячейка; или дом; или ящик, за которым свободная ячейка или дом
         * При этом, если на пути есть ящик, который может быть сдвинут, то необходимо переместить этот ящик на новые координаты.
         * Все объекты перемещаются на фиксированное значение FIELD_CELL_SIZE, независящее от размеров объекта, которые используются для его отрисовки.
         */

        for (Box box : gameObjects.getBoxes()) {
            if (gameObjects.getPlayer().isCollision(box, direction)) {
                for (Box item : gameObjects.getBoxes()) {
                    if (!box.equals(item)) {
                        if (box.isCollision(item, direction)) {
                            return true;
                        }
                    }
                    if (checkWallCollision(box, direction)) {
                        return true;
                    }
                }
                int dx = direction == Direction.LEFT ? -FIELD_CELL_SIZE : (direction == Direction.RIGHT ? FIELD_CELL_SIZE : 0);
                int dy = direction == Direction.UP ? -FIELD_CELL_SIZE : (direction == Direction.DOWN ? FIELD_CELL_SIZE : 0);
                box.move(dx, dy);
            }
        }
        return false;
    }

    public void checkCompletion() {
        /**тот метод проверяет пройден ли уровень
         * на всех ли домах стоят ящики, их координаты должны совпадать
         * Если условие выполнено, то проинформировать слушателя событий, что текущий уровень завершен.
         */

        int numberOfHomes = gameObjects.getHomes().size();
        int numberOfHomesWithBox = 0;

        for (Home home : gameObjects.getHomes()) {
            for (Box box : gameObjects.getBoxes()) {
                if (box.getX() == home.getX() && box.getY() == home.getY()) {
                    numberOfHomesWithBox++;
                }
            }
        }

        if (numberOfHomesWithBox == numberOfHomes) {
            eventListener.levelCompleted(currentLevel);
        }
    }

    public void restartLevel(int level) {
        //получает новые игровые объекты для указанного уровня у загрузчика уровня levelLoader и сохранять их в поле gameObjects.
        gameObjects = levelLoader.getLevel(level);
    }

    public GameObjects getGameObjects() {
        //возвращает все игровые объекты.
        return gameObjects;
    }

}
