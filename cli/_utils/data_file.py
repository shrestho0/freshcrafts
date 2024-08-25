
import os
import json

class DataFile:

    def __init__(self, data_file_location, logger):
        self.DATA_FILE_LOCATION = data_file_location
        self.LOGGER = logger

    def toJson(self, dictObj):
        # dict to json
        dict_to_json = json.dumps(dictObj)
        return dict_to_json

    def fromJson(self, jsonstr):
        # json to dict
        dict_from_json = json.loads(jsonstr)
        return dict_from_json

    def throw_if_nofile(self):
        if not os.path.exists(self.DATA_FILE_LOCATION):
            self.LOGGER.error(f"Data file not found at {self.DATA_FILE_LOCATION}")
            raise Exception(f"Data file not found at {self.DATA_FILE_LOCATION}")
        
    def export_data(self):
        try:
            with open(self.DATA_FILE_LOCATION, "w") as f:
                f.write(self.toJson())
        except Exception as e:
            self.LOGGER.error(f"Error while exporting data: {e}")

    def import_data(self):
        try:
            with open(self.DATA_FILE_LOCATION, "r") as f:
                self.fromJson(f.read())
        except Exception as e:
            self.LOGGER.error(f"Error while importing data: {e}")
