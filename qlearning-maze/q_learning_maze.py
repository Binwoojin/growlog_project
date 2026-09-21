"""
Q-Learning 미로 탐색 학습 프로그램 (25 x 25)
=================================================

[이 프로그램이 하는 일]
25 x 25 크기의 격자 미로에서 에이전트(파란 점)가
왼쪽 위(시작점)에서 오른쪽 아래(목표점)까지 가는 길을
"Q-Learning" 이라는 강화학습(Reinforcement Learning) 알고리즘으로
스스로 학습하는 과정을 화면으로 보여준다.

프로그램을 실행할 때마다 함정(❌)은 시작점/목표점을 제외한 칸 중에서
무작위로 7곳에 새로 배치된다.

[강화학습 핵심 용어 미리 정리]
- 상태(state)      : 에이전트가 현재 있는 위치. 여기서는 (행, 열) 좌표.
- 행동(action)     : 에이전트가 할 수 있는 선택. 여기서는 상/하/좌/우 이동 4가지.
- 보상(reward)     : 행동을 했을 때 환경이 주는 점수. 목표 도달=+100, 함정=-100, 한 걸음=-1.
- Q-table         : 모든 (상태, 행동) 조합에 대해 "그 행동이 얼마나 좋은가"를
                    숫자로 저장해 둔 표. 학습이 진행될수록 이 표의 값들이 점점
                    실제로 좋은 행동일수록 커지도록 갱신된다.
- 시도(attempt)    : 시작점에서 출발해서 목표에 도착하거나, 함정을 밟거나,
                    최대 스텝 수를 넘길 때까지의 한 번의 움직임 묶음.
                    함정을 밟거나 스텝이 초과되면 시작점으로 돌아가 같은
                    에피소드 안에서 "다시 시도"한다.
- 에피소드(episode) : 실제로 "목표 지점에 도달"해야만 끝나는 한 번의 학습 단위.
                    화면에 표시되는 에피소드 번호는 목표에 도착했을 때만 올라간다.
                    (에피소드 하나를 완료하기까지 함정에 걸려 여러 번 "시도"를
                    반복할 수 있다.)
- 엡실론-그리디(epsilon-greedy) : 학습 중 "탐험(exploration)"과 "활용(exploitation)"을
                    섞어서 행동을 고르는 전략. epsilon 확률로는 무작위로 움직여보고,
                    나머지 확률로는 지금까지 배운 것 중 가장 좋은 행동을 선택한다.
- 벨만 방정식(Bellman Equation) : Q-table을 갱신할 때 사용하는 핵심 공식.
                    "지금 받은 보상 + 미래에 받을 수 있는 최대 보상(할인 적용)"을
                    목표값으로 삼아 기존 Q값을 조금씩 그 목표값에 가깝게 수정한다.

[실행 방법]
    python q_learning_maze.py
(tkinter는 파이썬 표준 라이브러리이므로 별도 설치 없이 실행 가능하다.
 단, 일부 리눅스 배포판은 `sudo apt install python3-tk` 가 필요할 수 있다.)

[화면 하단 버튼 4개]
    1) 학습 시작        : Q-Learning 학습을 시작(또는 이어서 진행)한다.
    2) 학습 일시정지     : 진행 중인 학습을 멈춘다. (Q-table은 그대로 유지됨)
    3) 학습 초기화       : Q-table, 에피소드 수 등을 모두 지우고, 함정도 새로
                          무작위 배치하여 완전히 처음부터 다시 시작한다.
    4) 최적 경로 시스템   : 지금까지 학습된 Q-table을 바탕으로(더 이상 무작위
                          탐험 없이) 시작점에서 목표까지 가는 최적 경로를
                          애니메이션으로 보여준다.
"""

import random
import tkinter as tk

# =========================================================
# 1. 환경(미로) 설정값
# =========================================================
GRID_SIZE = 25        # 미로의 가로/세로 칸 수 -> 25 x 25
CELL_SIZE = 22         # 화면에 그릴 때 한 칸의 픽셀 크기
NUM_TRAPS = 7          # 함정 개수 (실행할 때마다 무작위 위치에 새로 배치)

START = (0, 0)                          # 시작점: 좌측 상단
GOAL = (GRID_SIZE - 1, GRID_SIZE - 1)   # 목표점: 우측 하단

# 에이전트가 선택할 수 있는 행동(상/하/좌/우) 목록
ACTIONS = ["UP", "DOWN", "LEFT", "RIGHT"]
# 각 행동이 (행, 열) 좌표를 어느 방향으로 얼마나 바꾸는지 정의
ACTION_DELTA = {
    "UP": (-1, 0),
    "DOWN": (1, 0),
    "LEFT": (0, -1),
    "RIGHT": (0, 1),
}

# =========================================================
# 2. Q-Learning 하이퍼파라미터
#    (강화학습 알고리즘의 동작 방식을 조절하는 값들)
# =========================================================
ALPHA = 0.1              # 학습률(learning rate): 새로운 정보를 얼마나 빨리 반영할지
GAMMA = 0.9              # 할인율(discount factor): 미래 보상을 현재 가치로 얼마나 인정할지
EPSILON_START = 1.0      # 학습 시작 시 탐험(무작위 행동) 확률 (100%)
EPSILON_MIN = 0.05       # epsilon이 아무리 줄어도 이 값 밑으로는 내려가지 않음(최소한의 탐험 유지)
EPSILON_DECAY = 0.9995   # 매 "시도"가 끝날 때마다 epsilon에 곱해서 조금씩 줄여나가는 비율
MAX_STEPS_PER_ATTEMPT = 600  # 한 번의 "시도"가 끝없이 이어지는 것을 막기 위한 최대 스텝 수

# 보상(reward) 설계: 강화학습에서 "어떤 행동을 좋게/나쁘게 볼지"를 정하는 부분
REWARD_GOAL = 100   # 목표 도달 시 큰 보상
REWARD_TRAP = -100  # 함정을 밟았을 때 큰 벌점
REWARD_STEP = -1    # 한 칸 이동할 때마다 -1 -> "최대한 짧은 경로"를 찾도록 유도
REWARD_WALL = -5    # 격자 바깥으로 나가려고 하면(제자리 유지) 약한 벌점


class MazeEnv:
    """
    25 x 25 격자 미로 환경.

    강화학습에서 "환경(Environment)"은 에이전트의 행동을 입력받아
    (다음 상태, 보상, 종료 여부)를 돌려주는 역할을 한다.
    """

    def __init__(self):
        self.traps = set()
        self.reset_traps()          # 프로그램 실행 시 함정을 무작위로 배치
        self.agent_pos = START

    def reset_traps(self):
        """시작점/목표점을 제외한 칸들 중에서 무작위로 NUM_TRAPS개를 함정으로 선택한다."""
        candidates = [
            (r, c)
            for r in range(GRID_SIZE)
            for c in range(GRID_SIZE)
            if (r, c) != START and (r, c) != GOAL
        ]
        self.traps = set(random.sample(candidates, NUM_TRAPS))

    def reset_agent(self):
        """에이전트를 시작점으로 되돌린다. (새 에피소드 시작할 때 호출)"""
        self.agent_pos = START
        return self.agent_pos

    def step(self, state, action):
        """
        환경과의 상호작용 한 번을 처리한다.
        현재 상태(state)에서 행동(action)을 수행했을 때
        (다음 상태, 보상, 이번 "시도" 종료 여부) 를 반환한다.

        주의: 여기서 반환하는 종료 여부(done)는 Q-Learning 갱신식(벨만 방정식)에서
        "더 이상 미래 가치가 없다"고 볼지를 결정하는 값일 뿐, 화면에 보이는
        "에피소드"가 끝났는지 여부와는 다르다. 목표 도달과 함정 모두 이 시도를
        끝내지만, 실제 에피소드(목표 도달)가 끝났는지는 next_state가 GOAL인지로
        호출하는 쪽(GUI)에서 별도로 판단한다.
        """
        row, col = state
        d_row, d_col = ACTION_DELTA[action]
        new_row, new_col = row + d_row, col + d_col

        # 격자를 벗어나는 행동이면 제자리에 머무르고 약한 벌점만 준다.
        if not (0 <= new_row < GRID_SIZE and 0 <= new_col < GRID_SIZE):
            return (row, col), REWARD_WALL, False

        next_state = (new_row, new_col)

        if next_state == GOAL:
            return next_state, REWARD_GOAL, True        # 목표 도달 -> 이번 시도 종료(성공)
        if next_state in self.traps:
            return next_state, REWARD_TRAP, True         # 함정 -> 이번 시도 종료(실패, 처음부터 재시도)
        return next_state, REWARD_STEP, False            # 일반 이동


class QLearningAgent:
    """
    Q-Learning 알고리즘을 수행하는 에이전트.

    Q(s, a) <- Q(s, a) + alpha * ( r + gamma * max_a' Q(s', a') - Q(s, a) )

    위 식이 Q-Learning의 핵심인 벨만 방정식 기반 갱신식이다.
    - Q(s, a)               : 상태 s에서 행동 a를 했을 때의 "예상 가치"
    - r                     : 방금 얻은 즉시 보상
    - max_a' Q(s', a')      : 다음 상태 s'에서 가장 좋은 행동을 했을 때의 예상 가치
    - alpha (학습률)         : 새로 계산한 값을 얼마나 빨리 반영할지
    - gamma (할인율)         : 미래 가치를 현재 시점에서 얼마나 쳐줄지
    """

    def __init__(self):
        # Q-table: q_table[행][열] = [UP, DOWN, LEFT, RIGHT] 각각의 가치(Q값)
        # 처음에는 아무것도 모르는 상태이므로 전부 0으로 초기화한다.
        self.q_table = [
            [[0.0 for _ in ACTIONS] for _ in range(GRID_SIZE)]
            for _ in range(GRID_SIZE)
        ]
        self.epsilon = EPSILON_START  # 탐험(exploration) 확률

    def choose_action(self, state):
        """
        엡실론-그리디(epsilon-greedy) 정책으로 행동을 선택한다.
        - epsilon 확률로 무작위 행동 선택 -> 탐험(Exploration): 새로운 길을 시도해본다.
        - (1 - epsilon) 확률로 Q값이 가장 큰 행동 선택 -> 활용(Exploitation): 배운 대로 움직인다.
        학습 초반에는 epsilon이 커서 탐험 위주이고, 학습이 진행될수록 epsilon이
        줄어들며 점점 "배운 대로" 움직이는 비중이 커진다.
        """
        if random.random() < self.epsilon:
            return random.choice(ACTIONS)
        return self.best_action(state)

    def best_action(self, state):
        """탐험 없이, 오직 Q값이 가장 큰(가장 좋다고 배운) 행동만 선택한다."""
        row, col = state
        q_values = self.q_table[row][col]
        max_q = max(q_values)
        return ACTIONS[q_values.index(max_q)]

    def update(self, state, action, reward, next_state, done):
        """벨만 방정식을 이용해 Q-table의 값을 한 걸음 더 정답에 가깝게 갱신한다."""
        row, col = state
        n_row, n_col = next_state
        action_index = ACTIONS.index(action)

        current_q = self.q_table[row][col][action_index]
        # 에피소드가 끝났다면(목표 도달/함정) 그 이후의 미래 가치는 없으므로 0으로 취급
        max_future_q = 0.0 if done else max(self.q_table[n_row][n_col])

        td_target = reward + GAMMA * max_future_q   # 이번 경험을 통해 새로 추정한 목표값
        td_error = td_target - current_q             # 기존 예측과 목표값의 차이(오차)
        self.q_table[row][col][action_index] = current_q + ALPHA * td_error

    def decay_epsilon(self):
        """매 "시도"가 끝날 때마다 epsilon을 조금씩 줄여서 탐험 비중을 낮춘다."""
        self.epsilon = max(EPSILON_MIN, self.epsilon * EPSILON_DECAY)


class QLearningGUI(tk.Tk):
    """tkinter로 만든 GUI. 미로를 그려주고, 학습을 진행/제어하는 버튼들을 제공한다."""

    STEPS_PER_TICK = 4   # 화면을 한 번 갱신할 때마다 몇 스텝씩 학습을 진행할지 (속도 조절용)

    def __init__(self):
        super().__init__()
        self.title("Q-Learning 미로 탐색 (25 x 25)")
        self.resizable(False, False)

        self.env = MazeEnv()
        self.agent = QLearningAgent()

        self.state = self.env.reset_agent()
        self.episode = 1          # 목표 지점에 "실제로 도달한" 횟수 (화면에 표시되는 에피소드 번호)
        self.attempt_steps = 0    # 이번 에피소드 안에서, 마지막으로 시작점에서 출발한 뒤 지금까지의 스텝 수
        self.retry_count = 0      # 이번 에피소드 동안 함정/시간초과로 재시도한 횟수 (참고용 통계)

        self.is_training = False     # 지금 학습 루프가 돌고 있는지 여부
        self.after_id = None         # tkinter의 after() 예약 id (일시정지 시 취소하기 위해 보관)

        self.showing_path = False    # "최적 경로 시스템" 애니메이션이 진행 중인지 여부
        self.path_state = None
        self.path_visited = set()

        self._build_widgets()
        self._redraw()

    # ---------------------------------------------------
    # 화면 구성
    # ---------------------------------------------------
    def _build_widgets(self):
        canvas_size = GRID_SIZE * CELL_SIZE
        self.canvas = tk.Canvas(self, width=canvas_size, height=canvas_size, bg="white")
        self.canvas.pack(padx=10, pady=10)

        self.info_var = tk.StringVar()
        info_label = tk.Label(self, textvariable=self.info_var, font=("Malgun Gothic", 11))
        info_label.pack(pady=(0, 5))

        # ---- 버튼 4개를 담을 프레임: side=BOTTOM으로 하단에, fill을 주지 않아 가로 중앙에 위치 ----
        button_frame = tk.Frame(self)
        button_frame.pack(side=tk.BOTTOM, pady=15)

        self.btn_start = tk.Button(
            button_frame, text="학습 시작", width=14, command=self.start_training
        )
        self.btn_pause = tk.Button(
            button_frame, text="학습 일시정지", width=14, command=self.pause_training
        )
        self.btn_reset = tk.Button(
            button_frame, text="학습 초기화", width=14, command=self.reset_training
        )
        self.btn_path = tk.Button(
            button_frame, text="최적 경로 시스템", width=16, command=self.show_optimal_path
        )

        for btn in (self.btn_start, self.btn_pause, self.btn_reset, self.btn_path):
            btn.pack(side=tk.LEFT, padx=6)

        self._update_info_label()

    def _update_info_label(self):
        if self.showing_path:
            status = "최적 경로 확인 중"
        elif self.is_training:
            status = "학습 중"
        else:
            status = "일시정지"
        self.info_var.set(
            f"에피소드(목표 도달): {self.episode}   진행 스텝: {self.attempt_steps}   "
            f"재시도(함정/시간초과): {self.retry_count}   "
            f"탐험률(epsilon): {self.agent.epsilon:.3f}   상태: {status}"
        )

    def _redraw(self):
        """미로 전체(칸, 함정, 목표, 에이전트/경로)를 다시 그린다."""
        self.canvas.delete("all")

        for r in range(GRID_SIZE):
            for c in range(GRID_SIZE):
                x0, y0 = c * CELL_SIZE, r * CELL_SIZE
                x1, y1 = x0 + CELL_SIZE, y0 + CELL_SIZE

                if (r, c) in self.env.traps:
                    color = "#ff6b6b"       # 함정 = 빨간색
                elif (r, c) == GOAL:
                    color = "#51cf66"       # 목표 = 초록색
                elif (r, c) == START:
                    color = "#ffd43b"       # 시작점 = 노란색
                else:
                    color = "white"

                self.canvas.create_rectangle(x0, y0, x1, y1, fill=color, outline="#dee2e6")

        if self.showing_path:
            # 지금까지 지나온 최적 경로 칸들을 파란 테두리로 표시
            for (r, c) in self.path_visited:
                x0, y0 = c * CELL_SIZE, r * CELL_SIZE
                x1, y1 = x0 + CELL_SIZE, y0 + CELL_SIZE
                self.canvas.create_rectangle(x0, y0, x1, y1, outline="#1971c2", width=2)
            self._draw_agent(self.path_state, color="#0b7285")
        else:
            self._draw_agent(self.state, color="#1971c2")

    def _draw_agent(self, pos, color):
        row, col = pos
        pad = 3
        x0, y0 = col * CELL_SIZE + pad, row * CELL_SIZE + pad
        x1, y1 = x0 + CELL_SIZE - 2 * pad, y0 + CELL_SIZE - 2 * pad
        self.canvas.create_oval(x0, y0, x1, y1, fill=color, outline="")

    # ---------------------------------------------------
    # 버튼 1) 학습 시작
    # ---------------------------------------------------
    def start_training(self):
        if self.is_training or self.showing_path:
            return  # 이미 학습 중이거나 경로를 보여주는 중이면 무시
        self.is_training = True
        self._train_tick()

    def _train_tick(self):
        """
        tkinter의 after()를 이용해 반복 호출되는 학습 루프.
        tkinter는 메인 스레드에서만 화면을 안전하게 갱신할 수 있으므로,
        별도의 쓰레드를 쓰는 대신 after()로 짧은 간격마다 조금씩 학습을 진행한다.
        """
        if not self.is_training:
            return

        for _ in range(self.STEPS_PER_TICK):
            # 1) 엡실론-그리디로 행동 선택
            action = self.agent.choose_action(self.state)
            # 2) 환경에 행동을 적용해서 다음 상태/보상/시도 종료여부를 받음
            next_state, reward, attempt_done = self.env.step(self.state, action)
            # 3) 벨만 방정식으로 Q-table 갱신 (실제 "학습"이 일어나는 부분)
            self.agent.update(self.state, action, reward, next_state, attempt_done)

            self.state = next_state
            self.attempt_steps += 1
            reached_goal = next_state == GOAL

            if attempt_done or self.attempt_steps >= MAX_STEPS_PER_ATTEMPT:
                # 한 "시도"가 끝났다: 목표 도달 / 함정 / 최대 스텝 초과 중 하나.
                # epsilon은 시도가 끝날 때마다 줄여야 학습이 효율적으로 진행되므로
                # 목표 도달 여부와 상관없이 항상 감소시킨다.
                self.agent.decay_epsilon()
                self.attempt_steps = 0
                self.state = self.env.reset_agent()

                if reached_goal:
                    # ---- 핵심: 화면에 보이는 "에피소드" 번호는 실제로 목표에
                    #      도달했을 때만 올라간다. 함정에 걸리거나 시간이
                    #      초과되면 같은 에피소드 안에서 처음부터 다시 시도한다. ----
                    self.episode += 1
                    self.retry_count = 0
                else:
                    self.retry_count += 1
                break  # 이번 tick은 시도가 끝난 시점에서 마무리

        self._redraw()
        self._update_info_label()

        # 1ms 뒤 다음 tick 예약 -> 학습을 계속 진행하면서도 tkinter가 버튼 클릭 등
        # 다른 이벤트를 처리할 수 있는 틈을 준다.
        self.after_id = self.after(1, self._train_tick)

    # ---------------------------------------------------
    # 버튼 2) 학습 일시정지
    # ---------------------------------------------------
    def pause_training(self):
        self.is_training = False
        if self.after_id is not None:
            self.after_cancel(self.after_id)
            self.after_id = None
        self._update_info_label()

    # ---------------------------------------------------
    # 버튼 3) 학습 초기화
    # ---------------------------------------------------
    def reset_training(self):
        self.pause_training()
        self.showing_path = False

        self.env.reset_traps()              # 함정도 새로 무작위 배치 (새로운 실행처럼)
        self.state = self.env.reset_agent()
        self.agent = QLearningAgent()        # Q-table과 epsilon을 완전히 새로 초기화
        self.episode = 1
        self.attempt_steps = 0
        self.retry_count = 0

        self._redraw()
        self._update_info_label()

    # ---------------------------------------------------
    # 버튼 4) 최적 경로 시스템
    # ---------------------------------------------------
    def show_optimal_path(self):
        """
        지금까지 학습된 Q-table을 이용해, 더 이상 무작위 탐험 없이(epsilon 사용 안 함)
        시작점부터 목표점까지 "가장 좋다고 배운" 행동만 따라가며 경로를 보여준다.
        """
        self.pause_training()
        self.showing_path = True
        self.path_state = START
        self.path_visited = {START}
        self._redraw()
        self._update_info_label()
        self._animate_path(step_count=0)

    def _animate_path(self, step_count):
        if self.path_state == GOAL:
            self.showing_path = False
            self._update_info_label()
            return

        if step_count >= MAX_STEPS_PER_ATTEMPT:
            # 아직 학습이 부족해 목표에 도달하지 못하고 최대 스텝을 넘긴 경우
            self.showing_path = False
            self._update_info_label()
            return

        # 탐험 없이 순수하게 Q값이 가장 큰 행동만 선택 (활용, Exploitation)
        action = self.agent.best_action(self.path_state)
        next_state, _reward, _done = self.env.step(self.path_state, action)

        # 같은 칸을 이미 지나쳤다면(제자리를 맴도는 루프) -> 학습이 아직 부족한 것이므로 중단
        if next_state in self.path_visited and next_state != GOAL:
            self.showing_path = False
            self._update_info_label()
            return

        self.path_visited.add(next_state)
        self.path_state = next_state
        self._redraw()

        if next_state in self.env.traps:
            # 최적 경로라 판단했지만 함정에 도달한 경우 -> 아직 학습이 부족함을 보여줌
            self.showing_path = False
            self._update_info_label()
            return

        # 사람이 눈으로 따라갈 수 있는 속도로 한 걸음씩 애니메이션
        self.after_id = self.after(80, lambda: self._animate_path(step_count + 1))


if __name__ == "__main__":
    app = QLearningGUI()
    app.mainloop()
