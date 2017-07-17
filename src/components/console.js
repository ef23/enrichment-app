import React, { Component } from 'react';
import {withRouter} from "react-router-dom";
import axios from 'axios';
import '../css/console.css'
import Tags from 'react-material-tags';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import injectTapEventPlugin from 'react-tap-event-plugin';
injectTapEventPlugin();

const DeleteButton = (props) => {
    const deleteQuestion = () => { 
        var isOK = window.confirm('Are you sure you want to delete this question?');
        if (isOK){
            console.log(props.qkey)
            axios.post('/profile/delete', {
                key: props.qkey
            })
            .then((response)=>{
                window.location.reload();
            })
            .catch((error)=> {
                window.alert(error.response.data.message)
            });
        }
    }

    return (<button onClick={deleteQuestion} className="option">
                <i className="fa fa-trash" aria-hidden="true"></i>
            </button>)
};

const EditButton = (props) => {
    const editQuestion = () => {
        console.log(props.tags)
        var newText = "";
        while (newText === ""){
            newText = window.prompt('Modify the question (Tags on next prompt):', props.text);
        }
        if (newText !== null) {
            var newTags = "";
            while (newTags === ""){
                newTags = window.prompt('Modify tags (separate by comma):', props.tags);
            }
            if (newTags !== null) {
                newTags = newTags.split(',');
                axios.post('/profile/update', {
                    key: props.qkey,
                    newText: newText,
                    newTags: newTags
                })
                .then((response)=>{
                    window.location.reload();
                })
                .catch((error)=> {
                    window.alert(error.response.data.message)
                });
                }
        }
    }

    return (<button onClick={editQuestion} className="option">
                <i className="fa fa-pencil" aria-hidden="true"></i>
            </button>)
};

class QuestionTable extends Component {
    constructor(props){
        super(props);
        this.state = {
            questions: [],
            tags: [],
            filterText: "",
            loaded: null
        }

        this.handleAddition = this.handleAddition.bind(this);
    }

    componentDidMount(){
        axios.get('/profile/read')
            .then((response) => {
                console.log(response.data.questions);
                this.setState({
                    questions: response.data.questions,
                });
                axios.get('/profile/tags')
                    .then((response) => {
                        console.log(response.data.tags)
                        var tags = response.data.tags.map((tag) => {
                            return {label: tag};
                        })
                        this.setState({
                            tags: tags,
                            loaded: true
                        });
                    }).catch((error) => {
                        console.log(error)
                    });
            }).catch((error) => {
                console.log(error)
        });
    }

    handleAddition(tag){
        console.log('filler')
    }

    updateFilter(event){
        this.setState({filterText: event.target.value});
    }

    render(){   
        console.log (this.state.tags)
        let filteredQuestions = this.state.questions.filter(
            (question) => {
                return question.text.toLowerCase().indexOf(this.state.filterText.toLowerCase()) !== -1;
            }
        );
        return(
            this.state.loaded !== null ? (
            <div>
            <h1>Questions</h1>
                <span> Filter by text: </span>
                <input type="text" value={this.state.filterText} onChange={this.updateFilter.bind(this)}/> 
                
                <span> Filter by tags: </span>
                <span>
                <MuiThemeProvider>
                    <Tags sourceTags={this.state.tags} onlyFromSource={true}/>
                </MuiThemeProvider>
                </span>
                <ul>
                {filteredQuestions.map((question, index) => {
                  return (
                    <li className="list" key={index}>
                      {question.text} 
                      <span>
                      <DeleteButton qkey={question.key}/>
                      <EditButton qkey={question.key} text={question.text} tags={question.taglist}/>
                      </span>
                    </li>
                    )
                })}
                </ul>
            </div>) 
            : null
        )
    }
}

const CreateContainer = () => (
    <div>
        <CreateQuestionBox/>
        <CreateTagsBox/>
    </div>
    )

class CreateTagsBox extends Component {
     constructor(props){
        super(props);
        this.state = {
            message: null,
            tags: ""
        }

        this.handleTagsChange = this.handleTagsChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    //handle create
    handleSubmit(event){
        if (this.state.tags.length === 0){
            this.setState({message: "Please enter tags!"});
        } else {
            axios.post('/profile/create', {
                tag: this.state.tags
            }).then((response) => {
                console.log(response.data.message)
                if (response.data.message === 'success'){
                     this.setState({
                        message: "Created successfully!", 
                        tag: "", 
                    })
                } else {
                    this.setState({
                        message: response.data.message,
                        success: false
                    });
                }
            }).catch((error) => {
                this.setState({message: "Unable to connect to signup server!"});
            })
        }
        event.preventDefault();
    }

    handleTagsChange(event){
        this.setState({tags: event.target.value});
    }

    render() {
        const message = this.state.message === null ? 
                        (<div></div>) : 
                        this.state.success === true ? 
                            <div className="alert alert-success"> {this.state.message} </div> :
                            (<div className="alert alert-danger"> {this.state.message} </div>);
        return (
        <div>
            <h1> Add a Tag </h1>
            {message}
            <form onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <label>Tags</label>
                    <input type="text" className="form-control" onChange={this.handleTagsChange}></input>
                </div>
                <button type="submit" className="btn btn-warning btn-lg">Add</button>
            </form>
        </div>)
    }
}

class CreateQuestionBox extends Component { 
    constructor(props){
        super(props);
        this.state = {
            message: null,
            question: "",
            tags: "",
            success: null
        }

        this.handleQuestionChange = this.handleQuestionChange.bind(this);
        this.handleTagsChange = this.handleTagsChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    //handle create
    handleSubmit(event){
        if (this.state.question.length === 0){
            this.setState({message: "Please enter a question!"});
        }
        else if (this.state.tags.length === 0){
            this.setState({message: "Please enter tags!"});
        } else {
            axios.post('/profile/create', {
                question: this.state.question,
                tags: this.state.tags
            }).then((response) => {
                console.log(response.data.message)
                if (response.data.message === 'success'){
                     this.setState({
                        message: "Created successfully!",
                        question: "", 
                        tags: "", 
                        success: true
                    })
                    //TODO better fix than this
                     window.location.reload();
                } else {
                    this.setState({
                        message: response.data.message,
                        success: false
                    });
                }
            }).catch((error) => {
                this.setState({message: "Unable to connect to signup server!"});
            })
        }
        event.preventDefault();
    }

    handleQuestionChange(event){
        this.setState({question: event.target.value});
    }


    handleTagsChange(event){
        this.setState({tags: event.target.value});
    }

    render() {
        const message = this.state.message === null ? 
                        (<div></div>) : 
                        this.state.success === true ? 
                            <div className="alert alert-success"> {this.state.message} </div> :
                            (<div className="alert alert-danger"> {this.state.message} </div>);
        return (
        <div>
            <h1> Add a question </h1>
            {message}
            <form onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <label>Question</label>
                    <input type="text" className="form-control" onChange={this.handleQuestionChange}></input>
                    <label>Tags</label>
                    <input type="text" className="form-control" onChange={this.handleTagsChange}></input>
                </div>
                <button type="submit" className="btn btn-warning btn-lg">Add</button>
            </form>
        </div>)
    }
}

class Console extends Component {

    render() {
        return (
            <div className="container container-padding">
                <div className="row">
                    <div className="col-md-8">
                        <QuestionTable/>
                    </div>
                    <div className="col-md-4">
                        <CreateContainer/>
                    </div>
                </div>
            </div>
            )
    }
}

export default withRouter(Console)