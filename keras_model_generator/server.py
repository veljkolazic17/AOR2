from flask import Flask, request, Response, jsonify
from agent import Agent

application = Flask(__name__)
agent : Agent = None


@application.route("/model_init", methods=["POST"])
def model_init():
    gamma = request.json.get("gamma")
    epsilon = request.json.get("epsilon")
    alpha = request.json.get("alpha")
    input_dims = request.json.get("input_dims")
    n_actions = request.json.get("n_actions")
    mem_size = request.json.get("mem_size")
    batch_size = request.json.get("batch_size")
    epsilon_end = request.json.get("epsilon_end")

    agent = Agent(
        gamma=gamma,
        epsilon=epsilon,
        alpha=alpha,
        input_dims=input_dims,
        n_actions=n_actions,
        mem_size=mem_size,
        batch_size=batch_size,
        epsilon_end=epsilon_end
    )

    return jsonify(message=agent.q_eval.summary()), 200

@application.route("/choose_action", methods=["POST"])
def choose_action():
    state = request.json.get("state")

    action = agent.choose_action(state)
    return jsonify(message=str(action)), 200

@application.route("/remember",methods=["POST"])
def remember():
    state = request.json.get("state")
    action = request.json.get("action")
    reward = request.json.get("reward")
    new_state = request.json.get("new_state")
    done = request.json.get("done")

    agent.remember(state,action,reward,new_state,done)
    return Response()

@application.route("/learn", methods=["POST"])
def learn():
    agent.learn()
    return Response()
 
@application.route("/load", methods=["POST"])
def load():
    model_name = request.json.get("model_name")
    agent.model_file = model_name
    agent.load_model()

    return Response()

@application.route("/save", methods=["POST"])
def save():
    model_name = request.json.get("model_name")
    agent.model_file = model_name
    agent.save_model()

    return Response()


if __name__ == "__main__":
    application.run(debug=True)