import React, { Component } from 'react';
import AutoComplete from 'material-ui/AutoComplete';
import Chip from 'material-ui/Chip';
import ContentAdd from 'material-ui/svg-icons/content/add';

import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import injectTapEventPlugin from 'react-tap-event-plugin';
injectTapEventPlugin();

/**
tags special component
**/
class Tags extends Component{

  constructor(props){

    super(props);

    this.state={
      tags:props.defTags,
      value:"",
      errorText:null,
      sourceTags:props.sourceTags.slice() //copy of source tags - we will change it
    }

    //elimination existsing tags from source tags
    this.state.sourceTags=this.state.sourceTags.filter((tag)=>{

      return !props.defTags.find((dtag)=>{  return tag.label===dtag.label });//elemeny must not exists in def tags

    });

    //set error text value
    this.errorText=(this.props.textField && this.props.textField.errorText)?this.props.textField.errorText:"Value is required";

  }


  //remove tag
  handleRequestDelete(tagData,key){
    const tags=this.state.tags.slice();
    tags.splice(key,1);

    //ok but now add this tag to autocomplete if it was there before
    if (  this.props.sourceTags.find( (tag)=>tagData.label===tag.label )  ){

      //was there so add it again to have it in autocomplete
      const sourceTags=this.state.sourceTags.slice(); //new arr
      sourceTags.push(tagData);
      //update state
      this.setState({sourceTags:sourceTags});
    }
    this.setState({tags:tags});
    this.props.onRemove(tagData,tags);//we send to callback current tags
  }


  getContainerStyle(){

    return this.props.containerStyle;
  }

  getContainerClassName(){

    return this.props.containerClassName;
  }



  //after enter key press
  handleKeyPress(e){

    if (e.charCode===13){//enter

        this.add(e.target.value);

      }
  }

  //after click autocomplete
  handleAutocomplete(value){

      this.refs.textField.focus();

      if (typeof value !== "object" )
      return;

      //set state value
      this.handleTextValueChange(value.label);
  }

  //on button click
  buttonClickHandler(){

    this.add(this.state.value);

  }

  //text change
  handleTextValueChange(value){

    this.setState({value:value});

    if (this.state.errorText!==null && value.trim().length>0)
    this.hideInputError();

  }


  //checks if tag is on our tag list
  tagExistsInList(tag){


    return this.state.tags.find((e)=>{

      return e.label===tag;

    });

  }

  /*
  Checks if tag exists in source list
  @return boolean
  */
  tagInSourceList(tag){

    return this.props.sourceTags.find((source)=>{

        return source.label===tag;

    })?true:false;

  }

  //removes tag from autocomplete
  removeTagFromSourceTags(tag){

    const sourceTags=this.state.sourceTags.filter((e)=>{

      return e.label!==tag;

    });

    this.setState({sourceTags:sourceTags});

  }


  //add new tag
  add(value){

    if (value.trim().length===0)//empty
    {
      //set error info
      this.showInputError();
      return;
    }

    value=value.trim(); //remove spaces

    if (this.props.onlyFromSource){
      //so only tags exists in souce can be added
      if (!this.tagInSourceList(value))
      return this.showCannotError(); //not allowed tag
    }

    this.hideInputError();

    if (this.tagExistsInList(value))
    return this.setState({value:""}); //we have this tag -clear input

    //remove from autocomplete list
    this.removeTagFromSourceTags(value);

    let tags=this.state.tags;
    tags.unshift({label:value});

    this.setState({tags:tags,value:""}); //set new tags and clean value

    if (this.props.onAdd!==null)
    this.props.onAdd(this.state.tags[0],this.state.tags);//we send to callback current tags
  }

  /*
  Shows error when tah is nota allowed
  */
  showCannotError(){

      this.setState({errorText:this.props.onlyFromSourceErrorText});
  }

  /*
  Shows error when not value is inserted in TextField
  */
  showInputError(){
    this.setState({errorText:this.errorText});
  }

  hideInputError(){

    this.setState({errorText:null});
  }


  //render single tag
  renderTag(data,key){
    let {...otherChip} = this.props.chip;
    return (
      <Chip
        {...otherChip}
        key={key}
        onRequestDelete={() => {
          this.handleRequestDelete(data, key)
        }
      }
      >
        {data.label}
    </Chip>
    )

  }

  render(){

    const tags=this.state.tags.map((tag,index)=>{

        return this.renderTag(tag,index);

    });



    let button=null;
    //add button
    if (this.props.button!==null){


      let {child,style,...otherButtonProps}=this.props.button;

      let childEl=child?child:<ContentAdd />;//something inside button

      style={...style};
      style.verticalAlign="top";

      button=(
        <button onClick={this.buttonClickHandler.bind(this)}   >
        {childEl}
        </button>
      );

    }


    return (
    <MuiThemeProvider>
      <div>
        <div>
          <AutoComplete {...this.props.textField}
          ref="textField"
          disableFocusRipple={false}
          searchText={this.state.value}
          dataSourceConfig={{text:'label',value:'label'}}
          dataSource={this.state.sourceTags}
          onNewRequest={this.handleAutocomplete.bind(this)}
          onUpdateInput={this.handleTextValueChange.bind(this)}
          errorText={this.state.errorText}
          onKeyPress={this.handleKeyPress.bind(this)} />
          {button}
        </div>
        <div style={this.getContainerStyle()} className={this.getContainerClassName()} >
          {tags}
        </div>
        
      </div>
    </MuiThemeProvider>
    );

  }

}

//default values
Tags.defaultProps = {
  defTags: [],
  sourceTags:[],
  onlyFromSource:false,
  onlyFromSourceErrorText:"This tag is not allowed",
  containerClassName:"tags-container",
  containerStyle:{
    display:"flex",
    flexDirection: "row" ,
    flexWrap: "wrap"
  },
  textField:{id:(Math.random()*1000).toString(), maxSearchResults:5 },
  chip:null,
  onRemove:null,
  onAdd:null,
  button:{child:<ContentAdd />} //default + icon
};

export default Tags;
