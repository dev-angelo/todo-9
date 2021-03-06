package kr.codesquad.todo9.todo.domain.board;

import kr.codesquad.todo9.todo.domain.card.Card;
import kr.codesquad.todo9.todo.domain.column.Column;
import kr.codesquad.todo9.todo.domain.log.Log;
import kr.codesquad.todo9.todo.domain.user.User;
import kr.codesquad.todo9.todo.requestobject.MoveCardObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {

    private static final Logger log = LoggerFactory.getLogger(Board.class);

    private @Id Long id;
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Log> logs = new ArrayList<>();

    public Board(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addCard(int boardKey, String contents, User user) {
        List<Card> cards = columns.get(boardKey).getCards();
        cards.add(createCard(contents, user));
    }

    public void addLog(String action, String type, User user, String contents, int boardKey) {
        logs.add(createLog(action, type, user, contents, boardKey));
    }

    public void addLog(String action, String type, User user, String contents, int boardKey, int columnKey) {
        logs.add(createLog(action, type, user, contents, boardKey, columnKey));
    }

    public void addLog(String action, String type, User user, String contents, Long cardId, Long fromColumnId, Long toColumnId) {
        logs.add(createLog(action, type, contents, contents, cardId, cardId, fromColumnId, toColumnId, user));
    }

    public void updateCard(int boardKey, int columnKey, String contents, User user) {
        Card card = this.columns.get(boardKey).getCards().get(columnKey);
        this.addLog("edit", "card", user, contents, boardKey, columnKey);
        card.setContents(contents);
        card.setUpdatedAt(LocalDateTime.now());
        card.setUpdatedUserId(user.getId());
    }

    public void deleteCard(int boardKey, int columnKey, User user) {
        Card card = this.columns.get(boardKey).getCards().get(columnKey);
        this.addLog("delete", "card", user, null, boardKey, columnKey);
        LocalDateTime now = LocalDateTime.now();
        card.setArchived(true);
        card.setUpdatedAt(now);
        card.setArchivedAt(now);
        card.setUpdatedUserId(user.getId());
    }

    public void moveCard(int boardKey, int columnKey, User user, MoveCardObject moveCardObject) {
        Column fromColumn = this.columns.get(boardKey);
        Column toColumn = this.columns.get(moveCardObject.getAfterBoardKey());
        List<Card> fromColumnCards = fromColumn.getCards();
        int fromColumnCardsSize = fromColumnCards.size();

        // 제일 위에 있는 항목을 선택하기 위한 코드
        columnKey = fromColumnCardsSize > columnKey ? columnKey : fromColumnCardsSize - 1;
        Card card = fromColumnCards.get(columnKey);

        this.addLog("move", "card", user, card.getContents(), card.getId(), fromColumn.getId(), toColumn.getId());

        log.debug("afterColumnKey: {}", moveCardObject.getAfterColumnKey());
        log.debug("toColumnCards: {}", toColumn.getCards());
        fromColumnCards.remove(card);
        toColumn.getCards().add(Math.max(moveCardObject.getAfterColumnKey(), 0), card);
        card.setUpdatedAt(LocalDateTime.now());
        card.setUpdatedUserId(user.getId());
    }

    public Log getLastLog() {
        return this.logs.get(this.logs.size() - 1);
    }

    public Board sortBoard() {
        for (Column column : this.columns) {
            List<Card> cards = new ArrayList<>();
            for (Card card : column.getCards()) {
                if (!card.getArchived()) {
                    cards.add(card);
                }
            }
            Collections.sort(cards);
            column.setCards(cards);
        }
        Collections.reverse(this.getLogs());
        return this;
    }

    private Log createLog(String action, String type, User user, String contents, int boardKey) {
        List<Card> cards = this.columns.get(boardKey).getCards();
        return createLog(action,
                type,
                null,
                contents,
                null,
                cards.get(cards.size() - 1).getId(),
                (long) boardKey + 1,
                (long) boardKey + 1,
                user);
    }

    private Log createLog(String action, String type, User user, String contents, int boardKey, int columnKey) {
        Card card = this.columns.get(boardKey).getCards().get(columnKey);
        return createLog(action,
                type,
                card.getContents(),
                contents,
                card.getId(),
                card.getId(),
                (long) boardKey + 1,
                (long) boardKey + 1,
                user);
    }

    private Log createLog(String action,
                          String type,
                          String beforeCardContents,
                          String afterCardContents,
                          Long beforeCardId,
                          Long afterCardId,
                          Long fromColumnId,
                          Long toColumnId,
                          User user) {
        return new Log(action,
                type,
                beforeCardContents,
                afterCardContents,
                beforeCardId,
                afterCardId,
                fromColumnId,
                toColumnId,
                LocalDateTime.now(),
                user.getId());
    }

    private Card createCard(String contents, User user) {
        Card card = new Card();
        card.setContents(contents);
        card.setCreatedUserId(user.getId());
        card.setUpdatedUserId(user.getId());
        log.debug("new Card: {}", card);

        return card;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Board{" + "id=" + id + ", name='" + name + '\'' + ", columns=" + columns + ", logs=" + logs + '}';
    }

}
