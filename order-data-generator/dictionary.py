import random


class WordDict:
    word_dict = []
    strings = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

    def __init__(self):
        # read dictionary
        with open("data/dict.txt", "r") as f:
            for line in f.readlines():
                self.word_dict.append(line.strip('\n'))

    def create_word(self, word_len):
        words = []
        for _ in range(word_len):
            words.append(self.word_dict[random.randint(0, len(self.word_dict) - 1)])
        return " ".join(words)

    def create_str(self, str_len):
        return "".join(random.sample(self.strings, str_len))
