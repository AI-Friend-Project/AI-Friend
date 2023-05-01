import logging
from transformers.configuration_utils import PretrainedConfig
from transformers import GPT2Config

logger = logging.getLogger(__name__)

#KoGPT2
kogpt2_config = {
    "initializer_range": 0.02,
    "layer_norm_epsilon": 1e-05,
    "n_ctx": 1024,
    "n_embd": 768,
    "n_head": 12,
    "n_layer": 12,
    "n_positions": 1024,
    "vocab_size": 50000,
    "activation_function": "gelu"
}

def get_kogpt2_config():
    return GPT2Config.from_dict(kogpt2_config)