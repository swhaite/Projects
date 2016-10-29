#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <sys/wait.h>

int execute(char *args[])
{
	int index = 0;
	int status;
	
	
	while(args[index] != NULL) {//or if index exceeds lenght MITE be gud alraeady
		
		if(!strcmp(args[index], "<"))
		{
			freopen(args[index+1], "r", stdin);
			int i = index;
			while(args[i] != NULL && i < 18) {
				args[i] = args[i+2];
				i++;
			}
			args[19] = NULL;
			
		}
		else if(!strcmp(args[index],">")) {
			freopen(args[index+1], "w", stdout);
			int i = index;
                        while(args[i] != NULL && i < 18) {
                                args[i] = args[i+2];
                                i++;
                        }
                        args[19] = NULL;
                        

		}
		else if(!strcmp(args[index], "|")) {
			//create pipe

			int status;
			int pfd[2];
			pipe(pfd);
			int pid = fork();

			if(pid == 0) {
				dup2(pfd[0], 0);
				close(pfd[1]);
				char **args2 = &args[index+1];
				execute(args2);
				exit(0);
			}
			else {
				dup2(pfd[1], 1);
				close(pfd[0]);
				args[index] = NULL;
				execvp(args[0], args);
				waitpid(pid, &status, 0);
				exit(0);
			}
		}
		else index += 1;
	}
	execvp(args[0], args);
}

int getcmd(char *prompt, char *args[], int *background)
{

	int length, i = 0;
	char *token, *loc;
	char *line = (char*)malloc(sizeof(char)*200);
	size_t linecap = 0;

	printf("%s", prompt);
	length = getline(&line, &linecap, stdin);

	if (length <= 0) {
		exit(-1);
	}

	if ((loc = index(line, '&')) != NULL) {
		*background = 1;
		*loc = ' ';
	} else
	    *background = 0;
	
	while ((token = strsep(&line, " \t\n")) != NULL) {
		for (int j = 0; j < strlen(token); j++)
			if (token[j] <= 32)
				token[j] = '\0';
		if (strlen(token) > 0)
			args[i++] = token;
	}
	
	free(line);
	return i;

}


int main(void)
{
	char *args[20];
	int bg;
	pid_t pid;
	pid_t jobs[100];
	int numJobs = 0;
	char *history[100][20];
	int numHistory = 0;
	int status;
	
	for(int i = 0; i < 100; i++) {
		history[i][0] = NULL;
		jobs[i] = 0;
	}

	while(1) {
		bg = 0;

		for(int j = 0; j < 20; j++) {
			args[j] = NULL;
		}
		int cnt = getcmd(">>   ", args, &bg);

		if( args[0] == NULL ) continue;
		if(numJobs == 100 && bg) {
			printf("max number of background jobs reached");
			continue;
		}
		char *nums = &args[0][1];
		int num = atoi(nums);

		if(!strcmp(args[0], "history")) {
			if(history[numHistory+1][0] == NULL) {
				for(int i = 0; i < numHistory; i++) {
					printf("command %d : %s", i+1, history[i][0]);
					int j = 1;
					while(history[i][j] !=NULL) {
						printf(" %s", history[i][j]);
						j++;
					}
					printf("\n");
				}
			}
			else {
				for(int i = 0; i <100; i++) {
					printf("command %d : %s", i+1, history[(i+numHistory+1)%100][0]);
					int j = 1;
					while(history[(i+numHistory+1)%100][j] != NULL) {
						printf(" %s", history[(i+numHistory+1)%100][j]);
					}
					printf("\n");
				}
			}
		}
		else if(!strcmp(args[0], "cd")) {
			if(args[1]!= NULL) {
				if(chdir(args[1]))printf("no such directory\n");
			}
		}
		else if(!strcmp(args[0], "pwd")) {
			printf("%s\n",getcwd(0,0));
		}
		else if(!strcmp(args[0], "jobs")) {
			for(int i = 0; i < 100; i++) {
				if(jobs[i] != 0) {
					if(!waitpid(jobs[i], &status, WNOHANG)) {
						printf("active job pid: %d\n", (int)jobs[i]);
					}
					else {
						jobs[i] = 0;
						numJobs--;
					}
				}
			}
		}
		else if(!strcmp(args[0], "fg")) {
			waitpid(atoi(args[1]), &status, 0);
		}
		else if((args[0][0] == '!') && (num > 0) && (num <= 100)) {
			if(history[numHistory+1][0] == NULL) {
				for(int i = 0; i < 20; i++) {
					args[i] = history[num-1][i];
				}
			}
			else {
				for(int i = 0; i < 20; i++) {
					args[i] = history[(num+numHistory-1)%100][i];
				}
			}
			pid = fork();
			if(pid == 0) {
				execute(args);
				exit(0);
			}
			else {
				if(bg==0)waitpid(pid, &status, 0);
			}
		}
		else if(!strcmp(args[0], "exit")) exit(0);
		else {

		for(int i = 0; i < 20; i++) {
			history[numHistory][i] = args[i];
		}
		numHistory++; //need to take care of overflow
		numHistory = numHistory % 100;
		pid = fork();
		if(pid == 0)
		{
			execute(args);
			exit(0);
		}
		else
		{

			if( bg == 0 ) waitpid(pid, &status, 0);
			else {
				int i = 0;
				while(jobs[i]!= 0) i++;
				jobs[i] = pid;
				numJobs++;

			}
		}
		}
	}
}
