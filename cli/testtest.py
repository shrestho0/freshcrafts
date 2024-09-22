import os
import shutil
import zipfile
import tiktoken
import anthropic
from langchain.schema import Document
from langchain_community.document_loaders import TextLoader

# poc for ai code doc generation


skip_dirs = {
    'node_modules', '.git', '.cache', 'build', 'dist', '.next', '.svelte-kit',
    'out', '.nuxt', '.output', '.vuepress', '.docusaurus', '.gridsome',
    'coverage', '.nyc_output', 'logs', 'tmp','target',
    '.vscode', '.idea', '.DS_Store', '_pycache_','package-lock.json'
}

include_extensions = {
    '.js', '.jsx', '.ts', '.tsx', '.vue', '.svelte',
    '.md', '.mdx', '.astro', '.html'
}



def process_zip(zip_path):
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall("temp_codebase")
    
    documents = []
    file_structure = []
    
    for root, dirs, files in os.walk("temp_codebase"):
        dirs[:] = [d for d in dirs if d not in skip_dirs]
        
        rel_path = os.path.relpath(root, "temp_codebase")
        parts = rel_path.split(os.sep)
        level = len(parts) - 1
        indent = "  " * level
        folder = os.path.basename(root)
        
        if rel_path != '.':
            file_structure.append(f"{indent}{folder}/")
        
        for file in files:
            if file.startswith('.') and not file.startswith('.env'):
                continue
            if os.path.splitext(file)[1] in include_extensions:
                file_path = os.path.join(root, file)
                try:
                    loader = TextLoader(file_path, encoding='utf-8')
                    file_content = loader.load()
                    
                    for doc in file_content:
                        full_content = f"File: {file_path}\n\n{doc.page_content}"
                        documents.append(Document(page_content=full_content, metadata=doc.metadata))
                    
                    file_structure.append(f"{indent}  {file}")
                except Exception as e:
                    print(f"Error loading {file_path}: {str(e)}")
                    file_structure.append(f"{indent}  {file} (unreadable)")
    
    file_structure_doc = "\n".join(file_structure)
    file_structure_document = Document(page_content=file_structure_doc, metadata={"source": "file_structure"})
    
    documents.insert(0, file_structure_document)
    
    return documents

def cleanup_temp_files():
    if os.path.exists("temp_codebase"):
        shutil.rmtree("temp_codebase")

ANTHROPIC_API_KEY = "__________________________"

anthropic_client = anthropic.Anthropic(
    api_key=ANTHROPIC_API_KEY
)

def generate_text_anthropic(content: str, system_promt: str):
    if not anthropic_client:
        raise Exception("Anthropic client not initialized")
    
    message = anthropic_client.messages.create(
        model="claude-3-5-sonnet-20240620",
        max_tokens=8192,
        temperature=0,
        system=system_promt,
        messages=[
            {"role": "user", "content": content}
        ]
    )
    return message.content

def num_tokens_from_string(string: str, encoding_name: str = "cl100k_base") -> int:
    """Returns the number of tokens in a text string."""
    encoding = tiktoken.get_encoding(encoding_name)
    num_tokens = len(encoding.encode(string))
    return num_tokens

zip_path = "test-cockpit.zip"
documents = process_zip(zip_path)
context = ""
for i in documents:
    context += (i.page_content + "\n\n") 


query = "make a documentation for the given code"

system_prompt = f"""You are an AI assistant tasked with generating documentation for a given codebase based on a user's query. Your goal is to analyze the provided code and file structure, and create clear, concise, and relevant documentation that addresses the user's specific question or request.

First, you will be presented with the text of a codebase, which includes the file structure and all relevant code. Then, you will receive a user query asking for specific documentation.
To generate the documentation:
1. Carefully analyze the codebase text, paying attention to:
   - File structure
   - Function and class definitions
   - Important variables and data structures
   - Key algorithms or processes
   - Any comments or existing documentation within the code
2. Consider the user's query and identify which parts of the codebase are most relevant to answering their question or fulfilling their request.
3. Generate documentation that specifically addresses the user's query. This may include:
   - Explanations of relevant functions, classes, or modules
   - Descriptions of important algorithms or processes
   - Usage examples
   - Any limitations or considerations
   - Links or references to related parts of the codebase
4. Ensure your documentation is:
   - Clear and concise
   - Well-structured and easy to read
   - Accurate and based on the provided codebase
   - Directly relevant to the user's query
5. If the user's query cannot be fully answered based on the provided codebase, explain what information is missing or what assumptions you had to make.
6. If you need to include code snippets in your documentation, enclose them in <code> tags.
Present your generated documentation within <documentation> tags. Begin with a brief summary of what the documentation covers, followed by the detailed information.
Remember to focus on the specific aspects of the codebase that are relevant to the user's query, rather than providing a comprehensive documentation of the entire codebase."""

context = f"""Here is the codebase text:
<codebase>
{context}
</codebase>

Here is the user's query:
<query>
{query}
</query>"""

# documentation = generate_text_anthropic(system_promt=system_prompt,content=context)
# token_count = num_tokens_from_string(context)

# print("=================================================================================")

# print(f"The string contains {token_count} tokens.")
# print(documentation[0].text)
# for i in documents:
#     print(i.page_content)

print(context)