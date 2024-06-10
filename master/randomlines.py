import random
import string

def generate_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def generate_random_sentence(min_words, max_words, min_length, max_length):
    num_words = random.randint(min_words, max_words)
    return ' '.join(generate_random_string(random.randint(min_length, max_length)) for _ in range(num_words))

with open('./myftpclient/random_lines.txt', 'w') as f:
    for _ in range(52):
        random_sentence = generate_random_sentence(1, 15, 1, 10)
        f.write(random_sentence + '\n')